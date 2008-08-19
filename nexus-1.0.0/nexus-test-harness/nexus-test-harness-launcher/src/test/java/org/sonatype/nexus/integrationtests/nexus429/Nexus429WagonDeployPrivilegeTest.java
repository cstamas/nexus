package org.sonatype.nexus.integrationtests.nexus429;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.MavenDeployer;

import com.thoughtworks.xstream.XStream;

public class Nexus429WagonDeployPrivilegeTest
    extends AbstractPrivilegeTest
{
    private static final String TEST_RELEASE_REPO = "nexus-test-harness-release-repo";
    private static final String TEST_SNAPSHOT_REPO = "nexus-test-harness-snapshot-repo";

    public Nexus429WagonDeployPrivilegeTest()
    {
        super( TEST_RELEASE_REPO );
    }

    @Test
    public void doReleaseArtifactTest()
        throws IOException, ConnectionException, AuthenticationException, ResourceDoesNotExistException,
        AuthorizationException, ComponentLookupException, TransferFailedException, InterruptedException,
        CommandLineException, VerificationException
    {
        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false, false,
                     null, false, null );
        this.setTestRepositoryId(TEST_RELEASE_REPO);
        this.deployPrivWithWagon( gav );
    }

    @Test
    public void doSnapshotArtifactTest()
        throws IOException, ConnectionException, AuthenticationException, ResourceDoesNotExistException,
        AuthorizationException, ComponentLookupException, TransferFailedException, InterruptedException,
        CommandLineException, VerificationException
    {
        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0-SNAPSHOT", null, "xml", 0, new Date().getTime(), "", false,
                     false, null, false, null );
        this.setTestRepositoryId(TEST_SNAPSHOT_REPO);
        this.deployPrivWithWagon( gav );
    }

    private void deployPrivWithWagon( Gav gav )
        throws IOException, ConnectionException, AuthenticationException, ResourceDoesNotExistException,
        AuthorizationException, ComponentLookupException, TransferFailedException, InterruptedException,
        CommandLineException, VerificationException
    {
        this.resetTestUserPrivs();

        Verifier verifier = null;

        this.deleteFromRepository( this.getTestId() );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        // we need to delete the files...
        this.deleteFromRepository( this.getTestId() + "/" );

        // deploy
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // with pom should fail

        try
        {
            verifier =
                MavenDeployer.deployAndGetVerifier( gav, this.getTestRepositoryId(), fileToDeploy,
                                                    this.getOverridableFile( "settings.xml" ) );
            failTest( verifier );
        }
        catch ( VerificationException e )
        {
            // expected 401
        }

        // give deployment role
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // try again
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // if this fails it will throw an error
        try
        {
            verifier =
                MavenDeployer.deployAndGetVerifier( gav, this.getNexusTestRepoUrl(), fileToDeploy,
                                                    this.getOverridableFile( "settings.xml" ) );
            failTest( verifier );
        }
        catch ( VerificationException e )
        {
            // expected 401
        }

        // try again
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "T5" );

        // if this fails it will throw an error
        verifier =
            MavenDeployer.deployAndGetVerifier( gav, this.getNexusTestRepoUrl(), fileToDeploy,
                                                this.getOverridableFile( "settings.xml" ) );
        verifier.verifyErrorFreeLog();

        // do it again as an update, this should fail

        // if this fails it will throw an error
        try
        {
            verifier =
                MavenDeployer.deployAndGetVerifier( gav, this.getNexusTestRepoUrl(), fileToDeploy,
                                                    this.getOverridableFile( "settings.xml" ) );
            failTest( verifier );
        }
        catch ( VerificationException e )
        {
            // expected 401
        }

        // now the user should be able to redeploy
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "T3" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );
        // if this fails it will throw an error
        verifier =
            MavenDeployer.deployAndGetVerifier( gav, this.getNexusTestRepoUrl(), fileToDeploy,
                                                this.getOverridableFile( "settings.xml" ) );
        verifier.verifyErrorFreeLog();

        // make sure delete does not work
        Response response =
            RequestFacade.sendMessage( "content/repositories/" + this.getTestRepositoryId() + "/" + this.getTestId(),
                                       Method.DELETE );
        Assert.assertEquals( "Artifact should have been deleted", 401, response.getStatus().getCode() );

    }

    // FIXME: refactor AbstractMavenNexusIT and sub class this from there.

    /**
     * Workaround to get some decent logging when tests fail
     * 
     * @throws IOException
     */
    protected void failTest( Verifier verifier )
        throws IOException
    {
        File logFile = new File( verifier.getBasedir(), "log.txt" );
        String log = FileUtils.readFileToString( logFile );
        log += "\n";
        log += new XStream().toXML( verifier );
        Assert.fail( log );
    }

}
