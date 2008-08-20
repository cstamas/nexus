package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Assert;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class SearchMessageUtil
{

    private Logger log = Logger.getLogger( getClass() );
    
    private XStream xstream;

    public SearchMessageUtil()
    {
        super();
        xstream = new XStream();
        XStreamInitializer.initialize( xstream );
    }

    public Response doSearchFor( String query )
        throws Exception
    {
        String serviceURI = "service/local/data_index?q=" + query;

        return RequestFacade.doGetRequest( serviceURI );
    }

    @SuppressWarnings( "unchecked" )
    public List<NexusArtifact> searchFor( String query )
        throws Exception
    {
        String responseText = doSearchFor( query ).getEntity().getText();

        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, responseText, MediaType.APPLICATION_XML );

        SearchResponse searchResponde = (SearchResponse) representation.getPayload( new SearchResponse() );

        return searchResponde.getData();
    }

    public NexusArtifact searchForSHA1( String sha1 )
        throws Exception
    {
        String serviceURI = "service/local/identify/sha1/" + sha1;

        String responseText = RequestFacade.doGetRequest( serviceURI ).getEntity().getText();

        log.debug( "responseText: \n" + responseText );

        if ( StringUtils.isEmpty( responseText ) )
        {
            return null;
        }

        return (NexusArtifact) xstream.fromXML( responseText );
    }

    public void updateIndexes( String... repositories )
        throws Exception
    {

        for ( String repo : repositories )
        {
            String serviceURI = "service/local/data_index/repositories/" + repo + "/content";
            Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
            Status status = response.getStatus();
            Assert.assertEquals( status.getCode(), 200 );
        }

        // let s w8 a few time for indexes
        Thread.sleep( 1000 * repositories.length );
    }

    public void allowBrowsing( String repositoryName, boolean allowBrowsing )
        throws Exception
    {
        RepositoryResource repository = getRepository( repositoryName );

        repository.setBrowseable( allowBrowsing );

        saveRepository( repository, repositoryName );
    }

    public void allowSearch( String repositoryName, boolean allowSearch )
        throws Exception
    {
        RepositoryResource repository = getRepository( repositoryName );

        repository.setIndexable( allowSearch );

        saveRepository( repository, repositoryName );
    }

    private RepositoryResource getRepository( String repositoryName )
        throws IOException
    {
        String serviceURI = "service/local/repositories/" + repositoryName;
        String responseText = RequestFacade.doGetRequest( serviceURI ).getEntity().getText();

        RepositoryResourceResponse repository = (RepositoryResourceResponse) xstream.fromXML( responseText );
        return (RepositoryResource) repository.getData();
    }

    private void saveRepository( RepositoryResource repository, String repositoryName )
        throws IOException
    {
        String serviceURI = "service/local/repositories/" + repositoryName;

        RepositoryResourceResponse repositoryResponse = new RepositoryResourceResponse();
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", MediaType.APPLICATION_XML );
        repositoryResponse.setData( repository );
        representation.setPayload( repositoryResponse );

        Status status = RequestFacade.sendMessage( serviceURI, Method.PUT, representation ).getStatus();
        Assert.assertEquals( Status.SUCCESS_OK.getCode(), status.getCode() );

    }

    public void allowDeploying( String repositoryName, boolean allowDeploying )
        throws Exception
    {
        RepositoryResource repository = getRepository( repositoryName );

        repository.setAllowWrite( allowDeploying );

        saveRepository( repository, repositoryName );
    }

}
