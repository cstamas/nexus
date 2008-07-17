package org.sonatype.nexus.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class ReleaseDeployTest
    extends AbstractNexusIntegrationTest
{

    private static final String TEST_RELEASE_REPO = "nexus-test-harness-release-repo";

    public ReleaseDeployTest()
    {
        super( REPOSITORY_RELATIVE_URL + TEST_RELEASE_REPO +"/" );
    }

  

    @Test
    public void deploywithGavUsingRest()
        throws HttpException, IOException
    {

        Gav gav =
            new Gav( "org.sonatype.nexus.test." + this.getClass().getName(), "uploadWithGav", "1.0.0", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null, false, null  );

        // file to deploy
        File fileToDeploy =
            FileTestingUtils.getTestFile( this.getClass(), gav.getArtifactId() + "." + gav.getExtension() );

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
        assertTrue( artifact.exists() );

        // make sure it is what we expect.
        assertTrue( FileTestingUtils.compareFileSHA1s( fileToDeploy, artifact ) );

        this.complete();
    }
    
    
    @Test
    public void deploywithPomUsingRest()
        throws HttpException, IOException
    {

        Gav gav =
            new Gav( "org.sonatype.nexus.test." + this.getClass().getName(), "uploadWithGav", "1.0.0", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null, false, null );

        // file to deploy
        File fileToDeploy =
            FileTestingUtils.getTestFile( this.getClass(), gav.getArtifactId() + "." + gav.getExtension() );
        
        File pomFile =
            FileTestingUtils.getTestFile( this.getClass(), "pom.xml" );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";
            
        int status = DeployUtils.deployUsingPomWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy, pomFile );

        if ( status != HttpStatus.SC_CREATED )
        {
            Assert.fail( "File did not upload successfully, status code: " + status );
        }

        // download it
        File artifact = downloadArtifact( gav, "./target/downloaded-jars" );

        // make sure its here
        assertTrue( artifact.exists() );

        // make sure it is what we expect.
        assertTrue( FileTestingUtils.compareFileSHA1s( fileToDeploy, artifact ) );

        this.complete();
    }
    
    
  
    
}
