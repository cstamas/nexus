/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.nexus259;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.DeployUtils;

/**
 * Deploys a snapshot artifact using a wagon and REST (both gav and pom) REST should fail 
 */
public class Nexus259SnapshotDeployIT
    extends AbstractNexusIntegrationTest
{

    private static final String TEST_SNAPSHOT_REPO = "nexus-test-harness-snapshot-repo";

    public Nexus259SnapshotDeployIT()
    {
        super( TEST_SNAPSHOT_REPO );
    }

    @Test
    public void deployUsingRest()
        throws Exception
    {

        Gav gav =
            new Gav( this.getTestId(), "uploadWithGav", "1.0.0-SNAPSHOT", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, true, null, false, null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        int status = DeployUtils.deployUsingGavWithRest( uploadURL, TEST_SNAPSHOT_REPO, gav, fileToDeploy );

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
        catch ( FileNotFoundException e )
        {
            fileWasUploaded = false;
        }

        Assert.assertFalse( "The file was uploaded and it should not have been.", fileWasUploaded );

    }

    @Test
    public void deploywithPomUsingRest()
        throws Exception
    {

        Gav gav =
            new Gav( this.getTestId(), "uploadWithGav", "1.0.0-SNAPSHOT", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null, false, null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        File pomFile = this.getTestFile( "pom.xml" );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        int status = DeployUtils.deployUsingPomWithRest( uploadURL, TEST_SNAPSHOT_REPO, fileToDeploy, pomFile, null, null );

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
        catch ( FileNotFoundException e )
        {
            fileWasUploaded = false;
        }

        Assert.assertFalse( "The file was uploaded and it should not have been.", fileWasUploaded );
    }
}
