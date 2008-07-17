package org.sonatype.nexus.test.proxy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.test.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class RemoteRepoDownTest
    extends AbstractNexusIntegrationTest
{

    public static final String TEST_RELEASE_REPO = "release-proxy-repo-1";

    public RemoteRepoDownTest()
    {
        super( REPOSITORY_RELATIVE_URL + TEST_RELEASE_REPO + "/" );
    }

    @Test
    public void downloadFromDisconnectedProxy()
        throws IOException
    {
        ProxyRepo.stop();

        Gav gav =
            new Gav( this.getClass().getName(), "repo-down-test-artifact", "1.0.0", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null, false, null );

        File localFile = ProxyRepo.getInstance().getLocalFile( TEST_RELEASE_REPO, gav );

        // make sure this exists first, or the test is invalid anyway.
        Assert.assertTrue( "The File: " + localFile + " does not exist.", localFile.exists() );

        boolean testPassed = false;
        try
        {
            this.downloadArtifact( gav, "target/downloads" );
        }
        catch ( FileNotFoundException e )
        {
            testPassed = true;
        }
        Assert.assertTrue( "A FileNotFoundException should have been thrown.", testPassed );

        // Start up the proxy
        ProxyRepo.start();

        // should not be able to download artifact after starting proxy, without clearing the cache.
        testPassed = false;
        try
        {
            this.downloadArtifact( gav, "target/downloads" );
        }
        catch ( FileNotFoundException e )
        {
            testPassed = true;
        }
        Assert.assertTrue( "A FileNotFoundException should have been thrown.", testPassed );

        // unblock the proxy
        ProxyRepo.getInstance().setBlockProxy( this.getBaseNexusUrl(), TEST_RELEASE_REPO, false);

        File artifact = this.downloadArtifact( gav, "target/downloads" );

        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( artifact, localFile ) );
        this.complete();
    }

    private void clearProxyCache()
    {

        String serviceURI =
            this.getBaseNexusUrl() + "service/local/data_cache/repositories/" + TEST_RELEASE_REPO + "/content";

        Request request = new Request();

        request.setResourceRef( serviceURI );

        request.setMethod( Method.DELETE );

        Client client = new Client( Protocol.HTTP );

        Response response = client.handle( request );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not clear the cache for repo: " + TEST_RELEASE_REPO );
        }
    }
    

}
