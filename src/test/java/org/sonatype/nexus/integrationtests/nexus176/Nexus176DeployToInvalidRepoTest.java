package org.sonatype.nexus.integrationtests.nexus176;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.DeployUtils;

public class Nexus176DeployToInvalidRepoTest
    extends AbstractNexusIntegrationTest
{

    private static final String TEST_RELEASE_REPO = "invalid-release";

    public Nexus176DeployToInvalidRepoTest()
    {
        super( TEST_RELEASE_REPO );
    }

    @Test
    public void wagonDeployTest()
        throws Exception
    {
       
        Gav gav =
            new Gav( this.getClass().getName(), "simpleArtifact", "1.0.0", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null, false, null );

        // file to deploy
        File fileToDeploy =
            this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );
        
     // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        // the method we are calling
        PostMethod filePost = new PostMethod( uploadURL );
        filePost.getParams().setBooleanParameter( HttpMethodParams.USE_EXPECT_CONTINUE, true );

        int status = DeployUtils.deployUsingGavWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy );

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


    @Test
    public void deploywithGavUsingRest()
        throws HttpException, IOException
    {

        Gav gav =
            new Gav( this.getClass().getName(), "uploadWithGav", "1.0.0", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null, false, null  );

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
    
    

    @Test
    public void deploywithPomUsingRest()
        throws HttpException, IOException
    {

        Gav gav =
            new Gav( this.getClass().getName(), "uploadWithGav", "1.0.0", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null, false, null  );

        // file to deploy
        File fileToDeploy =
            this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );
        
        File pomFile =
            this.getTestFile( "pom.xml" );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";
            
        int status = DeployUtils.deployUsingPomWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy, pomFile );
        
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
    
    
    
    @Test
    public void wagonSnapshotDeployTest()
        throws Exception
    {
       
        Gav gav =
            new Gav( this.getClass().getName(), "simpleArtifact", "1.0.0-SNAPSHOT", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null, false, null  );

        // file to deploy
        File fileToDeploy =
            this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );
        
     // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        // the method we are calling
        PostMethod filePost = new PostMethod( uploadURL );
        filePost.getParams().setBooleanParameter( HttpMethodParams.USE_EXPECT_CONTINUE, true );

        int status = DeployUtils.deployUsingGavWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy );

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


    @Test
    public void deploySnapshotWithGavUsingRest()
        throws HttpException, IOException
    {

        Gav gav =
            new Gav( this.getClass().getName(), "uploadWithGav", "1.0.0-SNAPSHOT", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null, false, null  );

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
    
    

    @Test
    public void deploySnapshotWithPomUsingRest()
        throws HttpException, IOException
    {

        Gav gav =
            new Gav( this.getClass().getName(), "uploadWithGav", "1.0.0-SNAPSHOT", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null, false, null  );

        // file to deploy
        File fileToDeploy =
            this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );
        
        File pomFile =
            this.getTestFile( "pom.xml" );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";
            
        int status = DeployUtils.deployUsingPomWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy, pomFile );
        
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
