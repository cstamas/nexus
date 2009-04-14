package org.sonatype.nexus.mock;

import junit.framework.TestCase;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.NexusApplication;
import org.sonatype.nexus.rest.model.StatusResource;
import org.sonatype.nexus.rest.model.StatusResourceResponse;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;

import com.thoughtworks.xstream.XStream;

public class SimpleTest
    extends TestCase
{
    protected MockNexusEnvironment mockNexusEnvironment;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        mockNexusEnvironment = new MockNexusEnvironment( 12345, "/nexus" );

        mockNexusEnvironment.start();
    }

    protected void tearDown()
        throws Exception
    {
        mockNexusEnvironment.stop();

        super.tearDown();
    }

    /**
     * Here, we don't mock anything, we are relying on _real_ response from real Nexus
     *
     * @throws Exception
     */
    public void testStatusFine()
        throws Exception
    {
        MockHelper.getResponseMap().clear();

        Client client = new Client( Protocol.HTTP );

        Response response = client.get( new Reference( "http://localhost:12345/nexus/service/local/status" ) );

        assertEquals( 200, response.getStatus().getCode() );
    }

    /**
     * We mock the status resource to be unavailable.
     *
     * @throws Exception
     */
    public void testStatusUnavailable()
        throws Exception
    {
        MockHelper.getResponseMap().clear();

        MockHelper.getResponseMap().put( "/status", new MockResponse( Status.SERVER_ERROR_SERVICE_UNAVAILABLE, null ) );

        Client client = new Client( Protocol.HTTP );

        Response response = client.get( new Reference( "http://localhost:12345/nexus/service/local/status" ) );

        assertEquals( Status.SERVER_ERROR_SERVICE_UNAVAILABLE.getCode(), response.getStatus().getCode() );
    }

    /**
     * We mock status response.
     *
     * @throws Exception
     */
    public void testStatusCustomContent()
        throws Exception
    {
        MockHelper.getResponseMap().clear();

        StatusResourceResponse mockResponse = new StatusResourceResponse();

        StatusResource data = new StatusResource();

        data.setVersion( "DUMMY" );

        mockResponse.setData( data );

        MockHelper.getResponseMap().put( "/status", new MockResponse( Status.SUCCESS_OK, mockResponse ) );

        Client client = new Client( Protocol.HTTP );

        Response response = client.get( new Reference( "http://localhost:12345/nexus/service/local/status" ) );

        assertEquals( 200, response.getStatus().getCode() );

        NexusApplication na =
            (NexusApplication) mockNexusEnvironment.getPlexusContainer().lookup( Application.class, "nexus" );

        XStream xmlXstream = (XStream) na.getContext().getAttributes().get( PlexusRestletApplicationBridge.XML_XSTREAM );

        StatusResourceResponse responseUnmarshalled =
            (StatusResourceResponse) xmlXstream.fromXML( response.getEntity().getText(), new StatusResourceResponse() );

        assertEquals( mockResponse.getData().getVersion(), responseUnmarshalled.getData().getVersion() );
    }

}
