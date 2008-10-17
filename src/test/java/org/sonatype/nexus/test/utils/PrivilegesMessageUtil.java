package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.testng.Assert;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.NexusError;
import org.sonatype.nexus.rest.model.NexusErrorResponse;
import org.sonatype.nexus.rest.model.PrivilegeBaseResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeListResourceResponse;
import org.sonatype.nexus.rest.model.PrivilegeResourceRequest;
import org.sonatype.nexus.rest.model.PrivilegeStatusResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

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
    
    public List<PrivilegeBaseStatusResource> createPrivileges( PrivilegeBaseResource resource ) throws IOException
    {
        Response response = this.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create privilege: " + response.getStatus() );
        }

        // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.getResourceListFromResponse( response );
        SecurityConfigUtil.verifyRepoTargetPrivileges( statusResources );
        
        return statusResources;
    }

    public PrivilegeBaseStatusResource getPrivilegeResource( String id ) throws IOException
    {
        Response response = this.sendMessage( Method.GET, null, id );
        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not get Privilege: " + response.getStatus() +"\n" + response.getEntity().getText());
        }
        return this.getResourceFromResponse( response );
    }
    
    public Response sendMessage( Method method, PrivilegeBaseResource resource ) throws IOException
    {
        return this.sendMessage( method, resource, "" );
    }

    public Response sendMessage( Method method, PrivilegeBaseResource resource, String id ) throws IOException
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String privId = ( method == Method.POST ) ? "" : "/" + id;
        String serviceURI = "service/local/privileges" + privId;

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

    public PrivilegeBaseStatusResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        PrivilegeStatusResourceResponse resourceResponse =
            (PrivilegeStatusResourceResponse) representation.getPayload( new PrivilegeStatusResourceResponse() );

        return (PrivilegeBaseStatusResource) resourceResponse.getData();

    }

    public List<PrivilegeBaseStatusResource> getResourceListFromResponse( Response response )
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
        xstream.alias( "data", org.sonatype.nexus.rest.model.PrivilegeListResourceResponse.class );

        NexusErrorResponse errorResponse = (NexusErrorResponse) xstream.fromXML( xml, new NexusErrorResponse() );

        Assert.assertTrue( errorResponse.getErrors().size() > 0, "Error response is empty." );

        for ( Iterator<NexusError> iter = errorResponse.getErrors().iterator(); iter.hasNext(); )
        {
            NexusError error = (NexusError) iter.next();
            Assert.assertFalse( StringUtils.isEmpty( error.getMsg() ), "Response Error message is empty." );

        }

    }

}
