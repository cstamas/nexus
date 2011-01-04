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
package org.sonatype.nexus.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.representation.VelocityRepresentation;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "openSearchTemplate" )
@Path( "/opensearch" )
@Produces( "text/xml" )
public class OpenSearchTemplatePlexusResource
    extends AbstractNexusPlexusResource
{

    public OpenSearchTemplatePlexusResource()
    {
        super();
        setReadable( true );
        setModifiable( false );
    }

    @Override
    public Object getPayloadInstance()
    {
        // RO resource
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/opensearch";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        // the client should have index access for the search to work
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:index]" );
    }

    /**
     * Provides the OpenSearch description document for this Nexus instance. For the emitted XML, see <a
     * href="http://www.opensearch.org/Specifications/OpenSearch/1.1#OpenSearch_description_document">OpenSearch
     * Description Document</a>.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = String.class )
    public Representation get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Map<String, Object> map = new HashMap<String, Object>();

        Reference nexusRef = getContextRoot( request );
        String nexusRoot = nexusRef.toString();
        if ( nexusRoot.endsWith( "/" ) )
        {
            nexusRoot = nexusRoot.substring( 0, nexusRoot.length() - 1 );
        }

        map.put( "nexusRoot", nexusRoot );
        map.put( "nexusHost", nexusRef.getHostDomain() );

        VelocityRepresentation templateRepresentation =
            new VelocityRepresentation( context, "/templates/opensearch.vm", map, MediaType.TEXT_XML );

        return templateRepresentation;
    }
}
