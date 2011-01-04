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

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.ConfigurationsListResource;
import org.sonatype.nexus.rest.model.ConfigurationsListResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * A resource that is able to retrieve list of configurations.
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "configurationList" )
@Path( ConfigurationListPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class ConfigurationListPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String RESOURCE_URI = "/configs";
    
    @Override
    public Object getPayloadInstance()
    {
        // RO resource, no payload
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:configuration]" );
    }

    /**
     * Get the list of configuration files in Nexus.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = ConfigurationsListResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        ConfigurationsListResourceResponse result = new ConfigurationsListResourceResponse();
        
        Map<String, String> configFileNames = getNexus().getConfigurationFiles();
        
        for ( Map.Entry<String, String> entry : configFileNames.entrySet())
        {
            ConfigurationsListResource resource = new ConfigurationsListResource();
            
            resource.setResourceURI( createChildReference( request, this, entry.getKey() ).toString() );
            
            resource.setName( entry.getValue() );
            
            result.addData( resource );
        }

        return result;
    }
}
