package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Assert;

import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.StringRepresentation;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.StatusResourceResponse;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;

import com.thoughtworks.xstream.XStream;


/**
 * Simple util class
 *
 */
public class NexusStateUtil
{
    
    private static final String STATUS_STOPPED = "STOPPED";

    private static final String STATUS_STARTED = "STARTED";
    
    

    public static void sendNexusStatusCommand( String command ) throws IOException
    {
        
        Response response = RequestFacade.sendMessage( "service/local/status/command", Method.PUT, new StringRepresentation(command, MediaType.TEXT_ALL) );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not " + command + " Nexus: (" + response.getStatus() + ")" );
        }
    }
    
    public static StatusResourceResponse getNexusStatus() throws IOException
    {
        Response response = RequestFacade.doGetRequest( "service/local/status" );

        if ( !response.getStatus().isSuccess() )
        {
            throw new ConnectException( response.getStatus().toString() );
        }
        
        XStream xstream = XStreamInitializer.initialize( new XStream() );
        StatusResourceResponse status = null;

        status =
            (StatusResourceResponse) xstream.fromXML( response.getEntity().getText() );
        return status;
    }

    public static void doSoftStop() throws IOException
    {
        sendNexusStatusCommand( "STOP" );
    }

    public static void doSoftStart() throws IOException
    {
        sendNexusStatusCommand( "START" );
    }

    public static void doSoftRestart() throws IOException
    {
        sendNexusStatusCommand( "RESTART" );
    }

    public static boolean isNexusRunning()
        throws IOException
    {
        return ( STATUS_STARTED.equals( getNexusStatus().getData().getState() ) );
    }

    
}
