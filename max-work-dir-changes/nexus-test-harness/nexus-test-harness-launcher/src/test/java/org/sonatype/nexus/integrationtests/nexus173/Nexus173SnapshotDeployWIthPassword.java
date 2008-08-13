package org.sonatype.nexus.integrationtests.nexus173;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.MavenDeployer;

public class Nexus173SnapshotDeployWIthPassword
    extends AbstractPrivilegeTest
{

    private static final String TEST_RELEASE_REPO = "nexus-test-harness-snapshot-repo";

    public Nexus173SnapshotDeployWIthPassword()
    {
        super( TEST_RELEASE_REPO );
    }

    @Test
    public void deployWithMaven() throws IOException, InterruptedException, CommandLineException
    {

        // GAV
        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0-SNAPSHOT", null, "xml", 0, new Date().getTime(), "", false,
                     false, null, false, null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        // we need to delete the files...
        this.deleteFromRepository( this.getTestId() + "/" );

        // deploy
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // with pom should fail

        try
        {
            // DeployUtils.forkDeployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(), fileToDeploy,
            // this.getRelitiveArtifactPath( gav ));
            MavenDeployer.deploy( gav, this.getNexusTestRepoUrl(), fileToDeploy,
                                  this.getOverridableFile( "settings.xml" ) );
            Assert.fail( "File should NOT have been deployed" );
        }
        // catch ( TransferFailedException e )
        // {
        // // expected 401
        // }
        catch ( CommandLineException e )
        {
            // expected 401
            // MavenDeployer, either fails or not, we can't check the cause of the problem
        }

        // give deployment role
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        // this.giveUserPrivilege( "test-user", "T5" );
        this.giveUserPrivilege( "test-user", "T3" ); // the Wagon does a PUT not a POST, so this is correct

        // try again
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // if this fails it will throw an error
        MavenDeployer.deploy( gav, this.getNexusTestRepoUrl(), fileToDeploy, this.getOverridableFile( "settings.xml" ) );

        // make sure delete does not work
        Response response =
            RequestFacade.sendMessage( "content/repositories/" + this.getTestRepositoryId() + "/" + this.getTestId(),
                                       Method.DELETE );
        Assert.assertEquals( "Artifact should have been deleted", 401, response.getStatus().getCode() );

    }

}
