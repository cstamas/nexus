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
package org.sonatype.nexus.rest.repotargets;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.PatternSyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.proxy.target.Target;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.nexus.rest.model.RepositoryTargetListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RepositoryTargetListPlexusResource" )
@Path( RepositoryTargetListPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class RepositoryTargetListPlexusResource
    extends AbstractRepositoryTargetPlexusResource
{
    public static final String RESOURCE_URI = "/repo_targets";
    
    public RepositoryTargetListPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryTargetResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:targets]" );
    }

    /**
     * Get the list of configuration repository targets.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = RepositoryTargetListResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryTargetListResourceResponse result = new RepositoryTargetListResourceResponse();

        Collection<Target> targets = getTargetRegistry().getRepositoryTargets();

        RepositoryTargetListResource res = null;

        for ( Target target : targets )
        {
            res = new RepositoryTargetListResource();

            res.setId( target.getId() );

            res.setName( target.getName() );

            res.setContentClass( target.getContentClass().getId() );

            res.setResourceURI( this.createChildReference( request, this, target.getId() ).toString() );

            result.addData( res );
        }

        return result;
    }

    /**
     * Add a new repository target to nexus.
     */
    @Override
    @POST
    @ResourceMethodSignature( input = RepositoryTargetResourceResponse.class, output = RepositoryTargetResourceResponse.class )
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryTargetResourceResponse result = (RepositoryTargetResourceResponse) payload;
        RepositoryTargetResourceResponse resourceResponse = null;

        if ( result != null )
        {
            RepositoryTargetResource resource = result.getData();

            if ( validate( true, resource ) )
            {
                try
                {
                    // create
                    Target target = getRestToNexusResource( resource );
                    
                    getTargetRegistry().addRepositoryTarget( target );
                    
                    getNexusConfiguration().saveConfiguration();
                    
                    // response
                    resourceResponse = new RepositoryTargetResourceResponse();

                    resourceResponse.setData( result.getData() );
                }
                catch ( ConfigurationException e )
                {
                    // build an exception and throws it
                    handleConfigurationException( e );
                }
                catch ( PatternSyntaxException e )
                {
                    // TODO: fix because this happens before we validate, we need to fix the validation.
                    ErrorResponse errorResponse = getNexusErrorResponse( "*", e.getMessage() );
                    throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.", errorResponse );
                }
                catch ( IOException e )
                {
                    getLogger().warn( "Got IOException during creation of repository target!", e );

                    throw new ResourceException(
                        Status.SERVER_ERROR_INTERNAL,
                        "Got IOException during creation of repository target!" );
                }
            }
        }
        return resourceResponse;
    }
}
