package org.sonatype.nexus.integrationtests.nexus930;

import java.io.IOException;
import java.util.List;

import org.testng.Assert;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.PlexusComponentListResource;
import org.sonatype.nexus.rest.model.PlexusComponentListResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.testng.annotations.Test;

import com.thoughtworks.xstream.XStream;

/**
 * Test the AutoDiscoverComponent a
 */
public class Nexus930AutoDiscoverComponent
    extends AbstractPrivilegeTest
{

    @Test
    public void testInvalidRole()
        throws Exception
    {
        Response response1 = sendMessage( "JUNK-foo-Bar-JUNK", this.getXMLXStream(), MediaType.APPLICATION_XML );
        Assert.assertTrue( response1.getStatus().isClientError() );
        Assert.assertEquals( 404, response1.getStatus().getCode() );
    }

    @Test
    public void testContentClassComponentListPlexusResource()
        throws Exception
    {
        String role = "repo_content_classes";
        // do admin
        List<PlexusComponentListResource> result1 = this.getResult(
            role,
            this.getXMLXStream(),
            MediaType.APPLICATION_XML );
        Assert.assertTrue( result1.size() == 2,"Expected list size equal 2." );

        // 401 test
        this.overwriteUserRole( TEST_USER_NAME, "login-only" + role, "2" );
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );
        Response response = sendMessage( role, this.getXMLXStream(), MediaType.APPLICATION_XML );
        Assert.assertTrue( response
            .getStatus().isClientError(), "Expected Error: Status was: " + response.getStatus().getCode() );
        Assert.assertEquals( 401, response.getStatus().getCode() );

        // only content class priv
        this.overwriteUserRole( TEST_USER_NAME, "content-classes" + role, "70" );
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );
        response = sendMessage( role, this.getXMLXStream(), MediaType.APPLICATION_XML );
        Assert.assertTrue( response.getStatus().isSuccess() );
    }

    @Test
    public void testScheduledTaskTypeComonentListPlexusResource()
        throws Exception
    {
        String role = "schedule_types";
        // do admin
        List<PlexusComponentListResource> result1 = this.getResult(
            role,
            this.getXMLXStream(),
            MediaType.APPLICATION_XML );
        Assert.assertTrue(result1.size() > 1,  "Expected list larger then 1." );

        // 401 test
        this.overwriteUserRole( TEST_USER_NAME, "login-only" + role, "2" );
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );
        Response response = sendMessage( role, this.getXMLXStream(), MediaType.APPLICATION_XML );
        Assert.assertTrue( response
            .getStatus().isClientError(), "Expected Error: Status was: " + response.getStatus().getCode() );
        Assert.assertEquals( 401, response.getStatus().getCode() );

        // only content class priv
        this.overwriteUserRole( TEST_USER_NAME, "schedule_types" + role, "71" );
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );
        response = sendMessage( role, this.getXMLXStream(), MediaType.APPLICATION_XML );
        Assert.assertTrue( response.getStatus().isSuccess() );

    }

    private List<PlexusComponentListResource> getResult( String role, XStream xstream, MediaType mediaType )
        throws IOException
    {
        String responseString = this.sendMessage( role, xstream, mediaType ).getEntity().getText();

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        PlexusComponentListResourceResponse resourceResponse = (PlexusComponentListResourceResponse) representation
            .getPayload( new PlexusComponentListResourceResponse() );

        return (List<PlexusComponentListResource>) resourceResponse.getData();
    }

    private Response sendMessage( String role, XStream xstream, MediaType mediaType )
        throws IOException
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String serviceURI = "service/local/components/" + role;

        return RequestFacade.sendMessage( serviceURI, Method.GET, representation );
    }

}
