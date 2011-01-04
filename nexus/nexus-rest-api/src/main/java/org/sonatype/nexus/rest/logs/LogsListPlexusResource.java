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
package org.sonatype.nexus.rest.logs;

import java.io.IOException;
import java.util.Collection;

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
import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.LogsListResource;
import org.sonatype.nexus.rest.model.LogsListResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * The log file list resource handler. This handles the GET method only and simply returns the list of existing nexus
 * application log files.
 *
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "logsList" )
@Path( LogsListPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class LogsListPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String RESOURCE_URI = "/logs"; 
    @Override
    public Object getPayloadInstance()
    {
        // TODO Auto-generated method stub
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
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:logs]" );
    }

    /**
     * Get the list of log files on the server.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = LogsListResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        LogsListResourceResponse result = new LogsListResourceResponse();
        result.getData(); //just to load the data, prevent problem on js side

        try
        {
            Collection<NexusStreamResponse> logFiles = getNexus().getApplicationLogFiles();

            for ( NexusStreamResponse logFile : logFiles )
            {
                LogsListResource resource = new LogsListResource();

                resource.setResourceURI( createChildReference( request, this, logFile.getName() ).toString() );

                resource.setName( logFile.getName() );

                resource.setSize( logFile.getSize() );

                resource.setMimeType( logFile.getMimeType() );

                result.addData( resource );
            }
        }
        catch ( IOException e )
        {
            throw new ResourceException( e );
        }

        return result;
    }
}
