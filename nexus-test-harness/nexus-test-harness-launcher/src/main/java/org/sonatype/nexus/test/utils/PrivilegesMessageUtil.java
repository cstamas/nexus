/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;
import org.sonatype.security.rest.model.PrivilegeListResourceResponse;
import org.sonatype.security.rest.model.PrivilegeResource;
import org.sonatype.security.rest.model.PrivilegeResourceRequest;
import org.sonatype.security.rest.model.PrivilegeStatusResource;
import org.sonatype.security.rest.model.PrivilegeStatusResourceResponse;

import com.thoughtworks.xstream.XStream;

public class PrivilegesMessageUtil
{

    private XStream xstream;

    private MediaType mediaType;

    private Logger log = Logger.getLogger( getClass() );

    public PrivilegesMessageUtil( XStream xstream, MediaType mediaType )
    {
        super();
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    public List<PrivilegeStatusResource> createPrivileges( PrivilegeResource resource ) throws IOException
    {
        Response response = this.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create privilege: " + response.getStatus() );
        }

        // get the Resource object
        List<PrivilegeStatusResource> statusResources = this.getResourceListFromResponse( response );
        SecurityConfigUtil.verifyPrivileges( statusResources );

        return statusResources;
    }

    public PrivilegeStatusResource getPrivilegeResource( String id ) throws IOException
    {
        Response response = this.sendMessage( Method.GET, null, id );
        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not get Privilege: " + response.getStatus() +"\n" + response.getEntity().getText());
        }
        return this.getResourceFromResponse( response );
    }

    public List<PrivilegeStatusResource> getList(  ) throws IOException
    {
        Response response = this.sendMessage( Method.GET, null );
        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not get Privilege: " + response.getStatus() +"\n" + response.getEntity().getText());
        }
        return this.getResourceListFromResponse( response );
    }

    public Response sendMessage( Method method, PrivilegeResource resource ) throws IOException
    {
        return this.sendMessage( method, resource, "" );
    }

    public Response sendMessage( Method method, PrivilegeResource resource, String id ) throws IOException
    {
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String privId = ( method == Method.POST ) ? "" : "/" + id;
        String serviceURI = "service/local/privileges" + privId;
        
        if( method == Method.POST )
        {
            serviceURI += "_target";
        }

        if ( method == Method.POST || method == Method.PUT ) // adding put so we can check for the 405, without a resource you get a 400
        {
            PrivilegeResourceRequest requestResponse = new PrivilegeResourceRequest();
            requestResponse.setData( resource );

            // now set the payload
            representation.setPayload( requestResponse );
            log.debug( method.getName() + ": " + representation.getText() );
        }

        return RequestFacade.sendMessage( serviceURI, method, representation );
    }

    public PrivilegeStatusResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        PrivilegeStatusResourceResponse resourceResponse =
            (PrivilegeStatusResourceResponse) representation.getPayload( new PrivilegeStatusResourceResponse() );

        return resourceResponse.getData();

    }

    public List<PrivilegeStatusResource> getResourceListFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        PrivilegeListResourceResponse resourceResponse =
            (PrivilegeListResourceResponse) representation.getPayload( new PrivilegeListResourceResponse() );

        return resourceResponse.getData();
    }

    public void validateResponseErrorXml( String xml )
    {
        // to trick xstream
        // REMEMBER! You cannot use the XStreamInitializer 1:1 from Server!
        // It does n->1 mapping (maps different types to field data), while the client
        // has to do 1->n mapping (it knows what _will_ 'data' field contain)
        xstream.alias( "data", PrivilegeListResourceResponse.class );

        ErrorResponse errorResponse = (ErrorResponse) xstream.fromXML( xml, new ErrorResponse() );

        Assert.assertTrue( "Error response is empty.", errorResponse.getErrors().size() > 0 );

        for ( Iterator<ErrorMessage> iter = errorResponse.getErrors().iterator(); iter.hasNext(); )
        {
            ErrorMessage error = iter.next();
            Assert.assertFalse( "Response Error message is empty.", StringUtils.isEmpty( error.getMsg() ) );

        }

    }

}
