package org.sonatype.nexus.integrationtests.nexus429;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.DeployUtils;

public class Nexus429UploadArtifactPrivilegeTest
    extends AbstractPrivilegeTest
{
    private static final String TEST_RELEASE_REPO = "nexus-test-harness-release-repo";

    public Nexus429UploadArtifactPrivilegeTest()
    {
        super( TEST_RELEASE_REPO );
    }


    @Test
    public void deployPrivWithPom()
        throws IOException
    {
        // GAV
        Gav gav =
            new Gav( this.getTestId(), "uploadWithGav", "1.0.0", null, "xml", 0, new Date().getTime(), "", false,
                     false, null, false, null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        File pomFile = this.getTestFile( "pom.xml" );
         
        // deploy
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        // with pom should fail
        int status = DeployUtils.deployUsingPomWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy, pomFile );
        Assert.assertEquals( "Status should have been 401", 401, status );
                
        // give deployment role
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "65" );
        
        // try again
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        
        status = DeployUtils.deployUsingPomWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy, pomFile );
        Assert.assertEquals( "Status should have been 201", 201, status );
    }
    
    
    @Test
    public void deployPrivWithGav()
        throws IOException
    {
        // GAV
        Gav gav =
            new Gav( this.getTestId(), "uploadWithGav", "1.0.0", null, "xml", 0, new Date().getTime(), "", false,
                     false, null, false, null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );
         
        // deploy
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        // with gav should fail
        int status = DeployUtils.deployUsingGavWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy );
        Assert.assertEquals( "Status should have been 401", 401, status );
        
        // give deployment role
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "65" );
        
        // try again
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        
        status = DeployUtils.deployUsingGavWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy );
        Assert.assertEquals( "Status should have been 201", 201, status );

    }

}
