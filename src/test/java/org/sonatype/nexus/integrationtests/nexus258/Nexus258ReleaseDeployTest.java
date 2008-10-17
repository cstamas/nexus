package org.sonatype.nexus.integrationtests.nexus258;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.testng.Assert;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.testng.annotations.Test;


/**
 * Deploys a release artifact using a wagon and REST (both gav and pom) 
 */
public class Nexus258ReleaseDeployTest
    extends AbstractNexusIntegrationTest
{

    private static final String TEST_RELEASE_REPO = "nexus-test-harness-release-repo";

    public Nexus258ReleaseDeployTest()
    {
        super( TEST_RELEASE_REPO );
    }

  

    @Test
    public void deploywithGavUsingRest()
        throws HttpException, IOException
    {

        Gav gav =
            new Gav( this.getTestId(), "uploadWithGav", "1.0.0", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null, false, null  );

        // file to deploy
        File fileToDeploy =
            this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";
            
        int status = DeployUtils.deployUsingGavWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy );

        if ( status != HttpStatus.SC_CREATED )
        {
            Assert.fail( "File did not upload successfully, status code: " + status );
        }

        // download it
        File artifact = downloadArtifact( gav, "./target/downloaded-jars" );

        // make sure its here
        Assert.assertTrue( artifact.exists() );

        // make sure it is what we expect.
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( fileToDeploy, artifact ) );
    }
    
    
    @Test
    public void deployWithPomUsingRest()
        throws HttpException, IOException
    {

        Gav gav =
            new Gav( this.getTestId(), "uploadWithGav", "1.0.0", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null, false, null );

        // file to deploy
        File fileToDeploy =
            this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );
        
        File pomFile =
            this.getTestFile( "pom.xml" );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";
            
        int status = DeployUtils.deployUsingPomWithRest( uploadURL, TEST_RELEASE_REPO, fileToDeploy, pomFile, null, null );

        if ( status != HttpStatus.SC_CREATED )
        {
            Assert.fail( "File did not upload successfully, status code: " + status );
        }

        // download it
        File artifact = downloadArtifact( gav, "./target/downloaded-jars" );

        // make sure its here
        Assert.assertTrue( artifact.exists() );

        // make sure it is what we expect.
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( fileToDeploy, artifact ) );

    }
    
    
  
    
}
