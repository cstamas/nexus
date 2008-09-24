package org.sonatype.nexus.integrationtests.nexus168;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.DeployUtils;


/**
 * Deploy a snapshot artifact to a release repo. (should fail) 
 */
public class Nexus168SnapshotToReleaseTest
    extends AbstractNexusIntegrationTest
{

    private static final String TEST_RELEASE_REPO = "nexus-test-harness-release-repo";
    
    public Nexus168SnapshotToReleaseTest()
    {
        super( TEST_RELEASE_REPO );
    }

    @Test
    public void deployReleaseToSnapshot()
        throws Exception
    {
        Gav gav =
            new Gav( this.getTestId(), "simpleArtifact", "1.0.0-SNAPSHOT", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null, false, null );

        // file to deploy
        File fileToDeploy =
            this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        boolean testPassed = false;
        try
        {
            // deploy it
            // this should fail
            DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(), fileToDeploy,
                                         this.getRelitiveArtifactPath( gav ) );
        }
        catch ( AuthorizationException e )
        {
            // this is expected
            testPassed = true;
        }

        Assert.assertTrue( testPassed );
    }

    @Test
    public void deployUsingRest()
        throws HttpException, IOException
    {

        Gav gav =
            new Gav( this.getTestId(), "uploadWithGav", "1.0.0-SNAPSHOT", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, true, null, false, null );

        // file to deploy
        File fileToDeploy =
            this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        // the method we are calling
        PostMethod filePost = new PostMethod( uploadURL );
        filePost.getParams().setBooleanParameter( HttpMethodParams.USE_EXPECT_CONTINUE, true );

        int status = DeployUtils.deployUsingGavWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy );

        if ( status != HttpStatus.SC_BAD_REQUEST )
        {
            Assert.fail( "Snapshot repositories do not allow manual file upload: " + status );
        }

        boolean fileWasUploaded = true;
        try
        {
          // download it
          downloadArtifact( gav, "./target/downloaded-jars" );
        }
        catch(FileNotFoundException e)
        {
            fileWasUploaded = false;
        }
        
        Assert.assertFalse( "The file was uploaded and it should not have been.", fileWasUploaded );
    }
    
    
    @Test
    public void deploywithPomUsingRest()
        throws HttpException, IOException
    {

        Gav gav =
            new Gav( this.getTestId(), "uploadWithPom", "1.0.0-SNAPSHOT", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, true, null, false, null );

        // file to deploy
        File fileToDeploy =
            this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );
        
        File pomFile =
            this.getTestFile( "pom.xml" );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";
            
        int status = DeployUtils.deployUsingPomWithRest( uploadURL, TEST_RELEASE_REPO, fileToDeploy, pomFile, null, null );

        if ( status != HttpStatus.SC_BAD_REQUEST )
        {
            Assert.fail( "Upload attempt should have returned a 400, it returned:  "+ status);
        }
      
        boolean fileWasUploaded = true;
        try
        {
          // download it
          downloadArtifact( gav, "./target/downloaded-jars" );
        }
        catch(FileNotFoundException e)
        {
            fileWasUploaded = false;
        }
        
        Assert.assertFalse( "The file was uploaded and it should not have been.", fileWasUploaded );

    }
    
    
    
}
