/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.configurations;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.global.GlobalConfigurationPlexusResource;
import org.sonatype.plexus.rest.representation.InputStreamRepresentation;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * A resource that is able to retrieve configurations as stream.
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "configuration" )
@Path( "/configs/{configName}" )
@Produces( "text/xml" )
public class ConfigurationPlexusResource
    extends AbstractNexusPlexusResource
{
    /** The config key used in URI and request attributes */
    public static final String CONFIG_NAME_KEY = "configName";

    @Override
    public Object getPayloadInstance()
    {
        // this is RO resource, and have no payload, it streams the file to client
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/configs/{" + CONFIG_NAME_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/configs/*", "authcBasic,perms[nexus:configuration]" );
    }

    @Override
    public List<Variant> getVariants()
    {
        List<Variant> result = super.getVariants();

        result.clear();

        result.add( new Variant( MediaType.TEXT_PLAIN ) );

        result.add( new Variant( MediaType.APPLICATION_XML ) );

        return result;
    }

    /**
     * Returns the requested Nexus configuration. The keys for various configurations should be discovered by querying
     * the "/configs" resource first. This resource emits the raw configuration file used by Nexus as response body.
     * 
     * @param configKey The configuration key for which we want to get the configuration.
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( "configKey" ) }, output = String.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String key = request.getAttributes().get( GlobalConfigurationPlexusResource.CONFIG_NAME_KEY ).toString();

        try
        {
            NexusStreamResponse result;

            if ( !getNexus().getConfigurationFiles().containsKey( key ) )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "No configuration with key '" + key
                    + "' found!" );
            }
            else
            {
                result = getNexus().getConfigurationAsStreamByKey( key );
            }

            // TODO: make this real resource being able to be polled (ETag and last modified support)
            return new InputStreamRepresentation( MediaType.valueOf( result.getMimeType() ), result.getInputStream() );
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "IOException during configuration retrieval!", e );
        }
    }

}
