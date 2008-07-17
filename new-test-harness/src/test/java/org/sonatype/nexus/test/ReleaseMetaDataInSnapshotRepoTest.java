package org.sonatype.nexus.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import junit.framework.Assert;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;

public class ReleaseMetaDataInSnapshotRepoTest
    extends AbstractNexusIntegrationTest
{

    private static final String TEST_SNAPSHOT_REPO = "nexus-test-harness-snapshot-repo";

    private static final String TEST_GROUP = "nexus-test-harness-snapshot-group";

    public ReleaseMetaDataInSnapshotRepoTest()
    {
        super( REPOSITORY_RELATIVE_URL + TEST_SNAPSHOT_REPO + "/" );
    }

    @Test
    public void releaseMetaDataInSnapshotRepo()
        throws IOException
    {

        Gav gav =
            new Gav( this.getClass().getName(), "simple-artifact", "1.0.4", null, "jar", 0, new Date().getTime(),
                     "Simple Test Artifact", false, false, null, false, null );

        // try to download it
        boolean fileWasDownloaded = true;
        try
        {
            // download it
            downloadArtifactFromRepository( TEST_SNAPSHOT_REPO, gav, "./target/downloaded-jars" );
        }
        catch ( FileNotFoundException e )
        {
            fileWasDownloaded = false;
        }

        Assert.assertFalse( "The file was downloaded and it should not have been.", fileWasDownloaded );

        fileWasDownloaded = true;
        try
        {
            // download it
            downloadArtifactFromGroup( TEST_GROUP, gav, "./target/downloaded-jars" );
        }
        catch ( FileNotFoundException e )
        {
            fileWasDownloaded = false;
        }

        Assert.assertFalse( "The file was downloaded and it should not have been.", fileWasDownloaded );

        this.complete();
    }

    @Test
    public void metadataCleaningTest()
        throws IOException, XmlPullParserException
    {
        // now we are going to grab the maven-metadata.xml, and take a look at that, the release version should have
        // been stripped out.

        URL snapshotRepoMetaDataURL =
            new URL( this.getNexusURL() + this.getClass().getName().replace( '.', '/' )
                + "/simple-artifact/maven-metadata.xml" );
        URL groupMetaDataURL =
            new URL( this.getBaseNexusUrl() + GROUP_REPOSITORY_RELATIVE_URL + TEST_GROUP + "/"
                + this.getClass().getName().replace( '.', '/' ) + "/simple-artifact/maven-metadata.xml" );

        System.out.println( "snapshotRepoMetaDataURL: " + snapshotRepoMetaDataURL );
        System.out.println( "groupMetaDataURL: " + groupMetaDataURL );

        // // download the two meta data files
        // File snapshotRepoMetaDataFile = this.downloadFile( snapshotRepoMetaDataURL,
        // "./target/downloaded-jars/snapshotRepoMetaDataURL.xml" );
        // File groupMetaDataFile = this.downloadFile( groupMetaDataURL,
        // "./target/downloaded-jars/groupMetaDataFile.xml" );
        // these files should be the same
        // FIXME: add check

        // check the versions of the file
        MetadataXpp3Reader r = new MetadataXpp3Reader();

        InputStream is = snapshotRepoMetaDataURL.openStream();
        Metadata snapshotRepoMetaData = r.read( is );
        is.close();

        // TODO: we don't need to do the next 3 lines if line 86 works out
        is = groupMetaDataURL.openStream();
        Metadata groupMetaData = r.read( is );
        is.close();

        Assert.assertTrue(
                           "Metadata from snapshot repo does not have 2 versions, maybe you just fixed up the metadata merge code, in that case, change this test.",
                           snapshotRepoMetaData.getVersioning().getVersions().size() == 2 );
        Assert.assertTrue(
                           "Metadata from group does not have 2 versions, maybe you just fixed up the metadata merge code, in that case, change this test.",
                           groupMetaData.getVersioning().getVersions().size() == 2 );

        this.complete();

    }

}
