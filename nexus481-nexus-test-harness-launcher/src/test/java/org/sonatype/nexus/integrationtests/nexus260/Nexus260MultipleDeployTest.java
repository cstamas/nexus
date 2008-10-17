package org.sonatype.nexus.integrationtests.nexus260;


import java.io.File;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Deploys an artifact multiple times. (this is allowed)  
 */
public class Nexus260MultipleDeployTest
    extends AbstractNexusIntegrationTest
{

    public Nexus260MultipleDeployTest()
    {
        super( "nexus-test-harness-repo" );
    }


    @Test
    public void singleDeployTest()
        throws Exception
    {   
        // file to deploy
        File fileToDeploy = this.getTestFile( "singleDeployTest.xml" );

        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(),
                                     fileToDeploy, "org/sonatype/nexus-integration-tests/multiple-deploy-test/singleDeployTest/1/singleDeployTest-1.xml" );

        // download it
        File artifact = downloadArtifact( "org.sonatype.nexus-integration-tests.multiple-deploy-test", "singleDeployTest", "1", "xml", null, "./target/downloaded-jars" );

        // make sure its here
        Assert.assertTrue( artifact.exists() );

        // make sure it is what we expect.
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( fileToDeploy, artifact ) );
    }

    @Test
    public void deploySameFileMultipleTimesTest()
        throws Exception
    {
        // file to deploy
        File fileToDeploy = this.getTestFile("deploySameFileMultipleTimesTest.xml" );

        String deployPath = "org/sonatype/nexus-integration-tests/multiple-deploy-test/deploySameFileMultipleTimesTest/1/deploySameFileMultipleTimesTest-1.xml";

        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(),
                                     fileToDeploy, deployPath );

        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(),
                                     fileToDeploy, deployPath );
        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(),
                                     fileToDeploy, deployPath );

        // download it
        File artifact = downloadArtifact( "org.sonatype.nexus-integration-tests.multiple-deploy-test", "deploySameFileMultipleTimesTest", "1", "xml", null, "./target/downloaded-jars" );

        // make sure its here
        Assert.assertTrue( artifact.exists() );

        // make sure it is what we expect.
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( fileToDeploy, artifact ) );

    }

    @Test
    public void deployChangedFileMultipleTimesTest()
        throws Exception
    {
        // files to deploy
        File fileToDeploy1 = this.getTestFile( "deployChangedFileMultipleTimesTest1.xml" );
        File fileToDeploy2 = this.getTestFile(  "deployChangedFileMultipleTimesTest2.xml" );
        File fileToDeploy3 = this.getTestFile(  "deployChangedFileMultipleTimesTest3.xml" );

        String deployPath = "org/sonatype/nexus-integration-tests/multiple-deploy-test/deployChangedFileMultipleTimesTest/1/deployChangedFileMultipleTimesTest-1.xml";

        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(),
                                     fileToDeploy1, deployPath );

        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(),
                                     fileToDeploy2, deployPath );
        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(),
                                     fileToDeploy3, deployPath );

        // download it
        File artifact = downloadArtifact( "org.sonatype.nexus-integration-tests.multiple-deploy-test", "deployChangedFileMultipleTimesTest", "1", "xml", null, "./target/downloaded-jars" );

        // make sure its here
        Assert.assertTrue( artifact.exists() );

        // make sure it is what we expect.
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( fileToDeploy3, artifact ) );

        // this should pass if the above passed
        Assert.assertFalse( FileTestingUtils.compareFileSHA1s( fileToDeploy2, artifact ) );

    }

    
    @Test
    public void deploySameFileMultipleTimesUsingContentUriTest()
        throws Exception
    {
        
        // file to deploy
        File fileToDeploy = this.getTestFile("deploySameFileMultipleTimesUsingContentUri.xml" );

        String deployPath = "org/sonatype/nexus-integration-tests/multiple-deploy-test/deploySameFileMultipleTimesUsingContentUriTest/1/deploySameFileMultipleTimesUsingContentUriTest-1.xml";

        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoServiceUrl(),
                                     fileToDeploy, deployPath );

        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoServiceUrl(),
                                     fileToDeploy, deployPath );
        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getNexusTestRepoServiceUrl(),
                                     fileToDeploy, deployPath );

        // download it
        File artifact = downloadArtifact( "org.sonatype.nexus-integration-tests.multiple-deploy-test", "deploySameFileMultipleTimesUsingContentUriTest", "1", "xml", null, "./target/downloaded-jars" );

        // make sure its here
        Assert.assertTrue( artifact.exists() );

        // make sure it is what we expect.
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( fileToDeploy, artifact ) );

    }
    
    
}

