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
package org.sonatype.nexus.test.utils;

import java.io.IOException;

import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.configuration.model.CPathMappingItem;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.RepositoryRouteListResource;
import org.sonatype.nexus.rest.model.RepositoryRouteListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.sonatype.nexus.rest.model.RepositoryRouteResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;
import org.testng.Assert;

import com.thoughtworks.xstream.XStream;

public class RoutesMessageUtil
    extends ITUtil
{
    public static final String SERVICE_PART = RequestFacade.SERVICE_LOCAL + "repo_routes";

    private XStream xstream;

    private MediaType mediaType;

    private static final Logger LOG = Logger.getLogger( RoutesMessageUtil.class );

    public RoutesMessageUtil( AbstractNexusIntegrationTest test, XStream xstream, MediaType mediaType )
    {
        super( test );
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    public RepositoryRouteResource getRoute( String routeId )
        throws IOException
    {
        Response response = getRouteResponse( routeId );

        return this.getResourceFromResponse( response );
    }

    public Response getRouteResponse( String routeId )
        throws IOException
    {
        Response response = RequestFacade.doGetRequest( "service/local/repo_routes/" + routeId );
        return response;
    }

    public Response sendMessage( Method method, RepositoryRouteResource resource )
        throws IOException
    {
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String resourceId = ( resource.getId() == null ) ? "" : "/" + resource.getId();
        String serviceURI = "service/local/repo_routes" + resourceId;

        if ( method != Method.GET || method != Method.DELETE )
        {
            RepositoryRouteResourceResponse requestResponse = new RepositoryRouteResourceResponse();
            requestResponse.setData( resource );

            // now set the payload
            representation.setPayload( requestResponse );
        }

        return RequestFacade.sendMessage( serviceURI, method, representation );
    }

    public RepositoryRouteResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        LOG.debug( "responseText: " + responseString );

        Assert.assertFalse( StringUtils.isEmpty( responseString ), "Response text was empty." );
        Assert.assertTrue( response.getStatus().isSuccess(), response.getStatus() + "\n" + responseString );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        RepositoryRouteResourceResponse resourceResponse =
            (RepositoryRouteResourceResponse) representation.getPayload( new RepositoryRouteResourceResponse() );

        return resourceResponse.getData();
    }

    public void validateSame( List<RepositoryRouteMemberRepository> repos1, List<RepositoryRouteMemberRepository> repos2 )
    {
        Assert.assertEquals( repos1.size(), repos2.size() );

        for ( int ii = 0; ii < repos1.size(); ii++ )
        {
            RepositoryRouteMemberRepository repo1 = repos1.get( ii );
            RepositoryRouteMemberRepository repo2 = repos2.get( ii );
            this.validateSame( repo1, repo2 );
        }
    }

    public void validateSameRepoIds( List<RepositoryRouteMemberRepository> repos1, List<String> repos2 )
    {
        Assert.assertEquals( repos1.size(), repos2.size() );

        // this is ordered
        for ( int ii = 0; ii < repos1.size(); ii++ )
        {
            RepositoryRouteMemberRepository repo1 = repos1.get( ii );
            String repo2 = repos2.get( ii );
            Assert.assertEquals( repo1.getId(), repo2 );
        }
    }

    public void validateSame( RepositoryRouteMemberRepository repo1, RepositoryRouteMemberRepository repo2 )
    {
        // we only care about the Id field
        Assert.assertEquals( repo1.getId(), repo2.getId() );
    }

    public void validateRoutesConfig( RepositoryRouteResource resource )
        throws IOException
    {

        CPathMappingItem cRoute = getTest().getNexusConfigUtil().getRoute( resource.getId() );

        String msg =
            "Should be the same route. \n Expected:\n" + new XStream().toXML( resource ) + " \n \n Got: \n"
                + new XStream().toXML( cRoute );

        Assert.assertEquals( cRoute.getId(), resource.getId(), msg );
        Assert.assertEquals( cRoute.getGroupId(), resource.getGroupId(), msg );
        Assert.assertEquals( cRoute.getRoutePatterns(), Collections.singletonList( resource.getPattern() ), msg );
        Assert.assertEquals( cRoute.getRouteType(), resource.getRuleType(), msg );

        this.validateSameRepoIds( resource.getRepositories(), cRoute.getRepositories() );

    }

    public void validateResponseErrorXml( String xml )
    {

        ErrorResponse errorResponse = (ErrorResponse) xstream.fromXML( xml, new ErrorResponse() );

        Assert.assertTrue( errorResponse.getErrors().size() > 0, "Error response is empty." );

        for ( Iterator<ErrorMessage> iter = errorResponse.getErrors().iterator(); iter.hasNext(); )
        {
            ErrorMessage error = iter.next();
            Assert.assertFalse( StringUtils.isEmpty( error.getMsg() ), "Response Error message is empty." );

        }

    }

    @SuppressWarnings( "unchecked" )
    public static List<RepositoryRouteListResource> getList()
        throws IOException
    {
        String serviceURI = "service/local/repo_routes";

        Response response = RequestFacade.doGetRequest( serviceURI );
        Status status = response.getStatus();
        Assert.assertTrue( status.isSuccess(), "Unable to get routes: " + status.getDescription() );

        XStreamRepresentation representation =
            new XStreamRepresentation( XStreamFactory.getXmlXStream(), response.getEntity().getText(),
                                       MediaType.APPLICATION_XML );

        RepositoryRouteListResourceResponse resourceResponse =
            (RepositoryRouteListResourceResponse) representation.getPayload( new RepositoryRouteListResourceResponse() );

        return resourceResponse.getData();
    }

    public static void removeAllRoutes()
        throws IOException
    {
        List<RepositoryRouteListResource> routes = getList();
        for ( RepositoryRouteListResource route : routes )
        {
            Status status = delete( route.getResourceURI() ).getStatus();
            Assert.assertTrue( status.isSuccess(), "Unable to delete route: '" + route.getResourceURI() + "', due to: "
                                                       + status.getDescription() );
        }
    }

    public static Response delete( String resourceUri )
        throws IOException
    {
        return RequestFacade.sendMessage( new URL( resourceUri ), Method.DELETE, null );
    }

}
