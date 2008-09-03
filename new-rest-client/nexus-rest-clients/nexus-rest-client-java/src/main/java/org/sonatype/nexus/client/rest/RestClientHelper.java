package org.sonatype.nexus.client.rest;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.sonatype.nexus.client.NexusClientException;
import org.sonatype.nexus.client.NexusConnectionException;
import org.sonatype.nexus.rest.model.NexusResponse;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class RestClientHelper
{
    private Client restClient = new Client( Protocol.HTTP );

    private ChallengeResponse challenge;

    private String baseUrl;

    private static final String SERVICE_URL_PART = "service/local/";

    private Logger logger = Logger.getLogger( getClass() );

    public RestClientHelper( String baseUrl, String username, String password )
    {
        ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
        ChallengeResponse authentication = new ChallengeResponse( scheme, username, password );
        this.challenge = authentication;
        this.baseUrl = baseUrl;
    }

    private String buildUrl( String service, String id )
    {
        // build the url
        StringBuffer urlBuffer = new StringBuffer( this.baseUrl );

        // make sure we have a trailing / in the baseurl
        if ( !urlBuffer.toString().endsWith( "/" ) )
        {
            urlBuffer.append( "/" );
        }

        urlBuffer.append( SERVICE_URL_PART ).append( service );

        // if this is a POST we don't know the Id, otherwise we do.
        if ( id != null )
        {
            urlBuffer.append( "/" ).append( id );
        }
        return urlBuffer.toString();
    }

    public void delete( String service, String id )
        throws NexusClientException
    {
        String url = this.buildUrl( service, id );
        Response response = this.sendMessage( Method.DELETE, url, (Representation) null );

        if ( !response.getStatus().isSuccess() )
        {
            throw new NexusClientException( "Error in response from the server: " + response.getStatus() );
        }
        // thats it
    }

    public Object get( String service, String id )
        throws NexusClientException, NexusConnectionException
    {
        String url = this.buildUrl( service, id );
        return this.sendMessage( Method.GET, url, (NexusResponse) null );
    }

    public Object getList( String service )
        throws NexusClientException, NexusConnectionException
    {
        String url = this.buildUrl( service, "" );
        return this.sendMessage( Method.GET, url, (NexusResponse) null );
    }

    public Object create( String service, String id, NexusResponse nexusResponse )
        throws NexusConnectionException
    {
        return this.sendMessage( Method.POST, this.buildUrl( service, null ), nexusResponse );

        // FIXME: The repositories service doesn't return anything for create, see NEXUS-540
    }

    public Object update( String service, String id, NexusResponse nexusResponse )
        throws NexusConnectionException
    {
        return this.sendMessage( Method.PUT, this.buildUrl( service, id ), nexusResponse );
    }

    public Response sendMessage( Method method, String url, Representation representation )
    {
        logger.debug("Method: " + method.getName() + " url: " + url );

        Request request = new Request();
        request.setResourceRef( url );
        request.setMethod( method );
        request.setEntity( representation );
        request.setChallengeResponse( this.challenge );

        return this.restClient.handle( request );
    }

    public Object sendMessage( Method method, String url, NexusResponse nexusResponse )
        throws NexusConnectionException
    {
        XStream xstream = XStreamInitializer.initialize( new XStream() );
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", MediaType.APPLICATION_XML );

        // now set the payload
        representation.setPayload( nexusResponse );

        // get the response
        Response response = this.sendMessage( method, url, representation );

        // always expect a success
        if ( !response.getStatus().isSuccess() )
        {
            String errorMessage = "Error in response from server: " + response.getStatus() + ".";
            try
            {
                errorMessage += "\nResponse:\n" + response.getEntity().getText();
            }
            catch ( IOException e )
            {
                logger.warn( "Error getting the response text: " + e.getMessage(), e );
            }

            throw new NexusConnectionException( errorMessage );
        }

        Object result = null;
        try
        {
            String responseText = response.getEntity().getText();
            if ( StringUtils.isNotEmpty( responseText ) )
            {
                return xstream.fromXML( responseText );
            }
        }
        catch ( IOException e )
        {
            throw new NexusConnectionException( "Error getting response text: " + e.getMessage(), e );
        }
        return result;
    }

}
