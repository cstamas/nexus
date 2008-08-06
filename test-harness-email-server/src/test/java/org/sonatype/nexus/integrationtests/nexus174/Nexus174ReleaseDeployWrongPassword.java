package org.sonatype.nexus.integrationtests.nexus174;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class Nexus174ReleaseDeployWrongPassword
    extends AbstractNexusIntegrationTest
{

    private static final String TEST_RELEASE_REPO = "nexus-test-harness-release-repo";

    public Nexus174ReleaseDeployWrongPassword()
    {
        super( TEST_RELEASE_REPO );
    }

    @Test
    public void deploywithGavUsingRest()
        throws HttpException, IOException
    {


        // FIXME
        Assert.fail();
        
        Gav gav =
            new Gav( this.getTestId(), "uploadWithGav", "1.0.0", null, "xml", 0, new Date().getTime(),
                     "Simple Test Artifact", false, false, null, false, null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        int status = DeployUtils.deployUsingGavWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy );

        //TODO: set error code
        if ( status != HttpStatus.SC_FORBIDDEN )
        {
            Assert.fail( "File should not have been uploaded, status code: " + status );
        }
    }

    @Test
    public void deploywithPomUsingRest()
        throws HttpException, IOException
    {

        Gav gav =
            new Gav( this.getTestId(), "uploadWithGav", "1.0.0", null, "xml", 0, new Date().getTime(),
                     "Simple Test Artifact", false, false, null, false, null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        File pomFile = this.getTestFile( "pom.xml" );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        int status = DeployUtils.deployUsingPomWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy, pomFile );

        //TODO: set error code
        if ( status != HttpStatus.SC_CREATED )
        {
            Assert.fail( "File should not have been uploaded, status code: " + status );
        }

    }

}
