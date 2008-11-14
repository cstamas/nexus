package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class RepositoryMessageUtil
{

    private static final String NEXUS_STORAGE_END_PATH = "storage/";

    private XStream xstream;

    private MediaType mediaType;

    private static final Logger LOG = Logger.getLogger( RepositoryMessageUtil.class );

    public RepositoryMessageUtil( XStream xstream, MediaType mediaType )
    {
        super();
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    public RepositoryBaseResource createRepository( RepositoryBaseResource repo )
        throws IOException
    {

        Response response = this.sendMessage( Method.POST, repo );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not create Repository: " + response.getStatus() + ":\n" + responseText );
        }

        // // get the Resource object
        // RepositoryResource responseResource = this.getResourceFromResponse( response );

        // currently create doesn't return anything, it should see NEXUS-540
        // the work around is to call get at this point
        RepositoryBaseResource responseResource = this.getRepository( repo.getId() ); // GET always uses XML, due to a
        // problem in the RESTlet client

        this.validateResourceResponse( repo, responseResource );

        return responseResource;
    }

    public void validateResourceResponse( RepositoryBaseResource repo, RepositoryBaseResource responseResource )
        throws IOException
    {
        Assert.assertEquals( repo.getId(), responseResource.getId() );
        Assert.assertEquals( repo.getName(), responseResource.getName() );
        // Assert.assertEquals( repo.getDefaultLocalStorageUrl(), responseResource.getDefaultLocalStorageUrl() ); //
        // TODO: add check for this

        Assert.assertEquals( repo.getFormat(), responseResource.getFormat() );
        Assert.assertEquals( repo.getRepoType(), responseResource.getRepoType() );

        if ( repo.getRepoType().equals( "virtual" ) )
        {
            // check mirror
            RepositoryShadowResource expected = (RepositoryShadowResource) repo;
            RepositoryShadowResource actual = (RepositoryShadowResource) responseResource;

            Assert.assertEquals( expected.getShadowOf(), actual.getShadowOf() );
        }
        else
        {
            RepositoryResource expected = (RepositoryResource) repo;
            RepositoryResource actual = (RepositoryResource) responseResource;

            Assert.assertEquals( expected.getChecksumPolicy(), actual.getChecksumPolicy() );

            // TODO: sometimes the storage dir ends with a '/' SEE: NEXUS-542
            if ( actual.getDefaultLocalStorageUrl().endsWith( "/" ) )
            {
                Assert.assertTrue( "Unexpected defaultLocalStorage: <expected to end with> " + NEXUS_STORAGE_END_PATH
                    + repo.getId() + "/  <actual>" + actual.getDefaultLocalStorageUrl(), actual
                    .getDefaultLocalStorageUrl().endsWith( NEXUS_STORAGE_END_PATH + repo.getId() + "/" ) );
            }
            // NOTE one of these blocks should be removed
            else
            {
                Assert.assertTrue( "Unexpected defaultLocalStorage: <expected to end with> " + NEXUS_STORAGE_END_PATH
                    + repo.getId() + "  <actual>" + actual.getDefaultLocalStorageUrl(), actual
                    .getDefaultLocalStorageUrl().endsWith( NEXUS_STORAGE_END_PATH + repo.getId() ) );
            }

            Assert.assertEquals( expected.getNotFoundCacheTTL(), actual.getNotFoundCacheTTL() );
            Assert.assertEquals( expected.getOverrideLocalStorageUrl(), actual.getOverrideLocalStorageUrl() );

            if ( expected.getRemoteStorage() == null )
            {
                Assert.assertNull( actual.getRemoteStorage() );
            }
            else
            {
                Assert.assertEquals( expected.getRemoteStorage().getRemoteStorageUrl(), actual
                    .getRemoteStorage().getRemoteStorageUrl() );
            }

            Assert.assertEquals( expected.getRepoPolicy(), actual.getRepoPolicy() );
        }

        // check nexus.xml
        this.validateRepoInNexusConfig( responseResource );
    }

    public RepositoryBaseResource getRepository( String repoId )
        throws IOException
    {

        String responseText = RequestFacade
            .doGetRequest( "service/local/repositories/" + repoId ).getEntity().getText();
        LOG.debug( "responseText: \n" + responseText );

        // this should use call to: getResourceFromResponse
        XStreamRepresentation representation = new XStreamRepresentation(
            XStreamFactory.getXmlXStream(),
            responseText,
            MediaType.APPLICATION_XML );

        RepositoryResourceResponse resourceResponse = (RepositoryResourceResponse) representation
            .getPayload( new RepositoryResourceResponse() );

        return resourceResponse.getData();
    }

    public RepositoryBaseResource updateRepo( RepositoryBaseResource repo )
        throws IOException
    {
        Response response = this.sendMessage( Method.PUT, repo );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not update user: " + response.getStatus() + "\n" + responseText );
        }

        // this doesn't return any objects, it should....
        // // get the Resource object
        // RepositoryResource responseResource = this.getResourceFromResponse( response );

        // for now call GET
        RepositoryBaseResource responseResource = this.getRepository( repo.getId() );

        this.validateResourceResponse( repo, responseResource );

        return responseResource;
    }

    public Response sendMessage( Method method, RepositoryBaseResource resource, String id )
        throws IOException
    {
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String idPart = ( method == Method.POST ) ? "" : "/" + id;

        String serviceURI = "service/local/repositories" + idPart;

        RepositoryResourceResponse repoResponseRequest = new RepositoryResourceResponse();
        repoResponseRequest.setData( resource );

        // now set the payload
        representation.setPayload( repoResponseRequest );

        LOG.debug( "sendMessage: " + representation.getText() );

        return RequestFacade.sendMessage( serviceURI, method, representation );
    }

    public Response sendMessage( Method method, RepositoryBaseResource resource )
        throws IOException
    {
        return this.sendMessage( method, resource, resource.getId() );
    }

    /**
     * This should be replaced with a REST Call, but the REST client does not set the Accept correctly on GET's/
     *
     * @return
     * @throws IOException
     */
    @SuppressWarnings( "unchecked" )
    public List<RepositoryListResource> getList()
        throws IOException
    {
        String responseText = RequestFacade.doGetRequest( "service/local/repositories" ).getEntity().getText();
        LOG.debug( "responseText: \n" + responseText );

        XStreamRepresentation representation = new XStreamRepresentation(
            XStreamFactory.getXmlXStream(),
            responseText,
            MediaType.APPLICATION_XML );

        RepositoryListResourceResponse resourceResponse = (RepositoryListResourceResponse) representation
            .getPayload( new RepositoryListResourceResponse() );

        return resourceResponse.getData();

    }

    public RepositoryResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        LOG.debug( " getResourceFromResponse: " + responseString );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );
        RepositoryResourceResponse resourceResponse = (RepositoryResourceResponse) representation
            .getPayload( new RepositoryResourceResponse() );

        return (RepositoryResource) resourceResponse.getData();
    }

    private void validateRepoInNexusConfig( RepositoryBaseResource repo )
        throws IOException
    {

        if ( repo.getRepoType().equals( "virtual" ) )
        {
            // check mirror
            RepositoryShadowResource expected = (RepositoryShadowResource) repo;
            CRepositoryShadow cRepo = NexusConfigUtil.getRepoShadow( repo.getId() );

            Assert.assertEquals( expected.getShadowOf(), cRepo.getShadowOf() );
            Assert.assertEquals( expected.getId(), cRepo.getId() );
            Assert.assertEquals( expected.getName(), cRepo.getName() );
            Assert.assertEquals( expected.getFormat(), cRepo.getType() );
        }
        else
        {
            RepositoryResource expected = (RepositoryResource) repo;
            CRepository cRepo = NexusConfigUtil.getRepo( repo.getId() );

            Assert.assertEquals( expected.getId(), cRepo.getId() );
            Assert.assertEquals( expected.getChecksumPolicy(), cRepo.getChecksumPolicy() );
            Assert.assertEquals( expected.getName(), cRepo.getName() );
            Assert.assertEquals( expected.getFormat(), cRepo.getType() );
            Assert.assertEquals( expected.getNotFoundCacheTTL(), cRepo.getNotFoundCacheTTL() );
            Assert.assertEquals( expected.getOverrideLocalStorageUrl(), cRepo.getLocalStorage() );

            if ( expected.getRemoteStorage() == null )
            {
                Assert.assertNull( cRepo.getRemoteStorage() );
            }
            else
            {
                Assert.assertEquals( expected.getRemoteStorage().getRemoteStorageUrl(), cRepo
                    .getRemoteStorage().getUrl() );
            }

            Assert.assertEquals( expected.getRepoPolicy(), cRepo.getRepositoryPolicy() );
        }

    }

    public static void updateIndexes( String... repositories )
        throws Exception
    {

        for ( String repo : repositories )
        {
            String serviceURI = "service/local/data_index/repositories/" + repo + "/content";
            Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
            Status status = response.getStatus();
            Assert.assertTrue( "Fail to update " + repo + " repository index " + status, status.isSuccess() );
        }

        // let s w8 a few time for indexes
        Thread.sleep( 1000 * repositories.length );

    }

    public RepositoryStatusResource getStatus( String repoId )
        throws IOException
    {

        Response response = RequestFacade.sendMessage( RequestFacade.SERVICE_LOCAL + "repositories/" + repoId
            + "/status", Method.GET );
        Status status = response.getStatus();
        Assert.assertTrue( "Fail to getStatus for '" + repoId + "' repository" + status, status.isSuccess() );

        XStreamRepresentation representation = new XStreamRepresentation(
            this.xstream,
            response.getEntity().getText(),
            MediaType.APPLICATION_XML );

        RepositoryStatusResourceResponse resourceResponse = (RepositoryStatusResourceResponse) representation
            .getPayload( new RepositoryStatusResourceResponse() );

        return resourceResponse.getData();
    }

    public void updateStatus( RepositoryStatusResource repoStatus )
        throws IOException
    {
        String uriPart = RequestFacade.SERVICE_LOCAL + "repositories/" + repoStatus.getId() + "/status";

        XStreamRepresentation representation = new XStreamRepresentation( this.xstream, "", MediaType.APPLICATION_XML );
        RepositoryStatusResourceResponse resourceResponse = new RepositoryStatusResourceResponse();
        resourceResponse.setData( repoStatus );
        representation.setPayload( resourceResponse );

        Response response = RequestFacade.sendMessage( uriPart, Method.PUT, representation );
        Status status = response.getStatus();
        Assert.assertTrue( "Fail to update '" + repoStatus.getId() + "' repository status " + status +"\nResponse:\n"+ response.getEntity().getText() +"\nrepresentation:\n"+ representation.getText(), status
            .isSuccess() );

    }

}
