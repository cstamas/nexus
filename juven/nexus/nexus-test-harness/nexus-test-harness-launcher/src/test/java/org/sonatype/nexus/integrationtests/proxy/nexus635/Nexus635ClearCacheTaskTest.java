package org.sonatype.nexus.integrationtests.proxy.nexus635;

import static org.sonatype.nexus.test.utils.FileTestingUtils.compareFileSHA1s;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.proxy.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ClearCacheTaskDescriptor;
import org.sonatype.nexus.test.utils.MavenDeployer;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * Tests the clear cache task.
 */
public class Nexus635ClearCacheTaskTest
    extends AbstractNexusProxyIntegrationTest
{

    private static final Gav GAV =
        new Gav( "nexus635", "artifact", "1.0-SNAPSHOT", null, "jar", 0, 0L, null, true, false, null, false, null );

    public Nexus635ClearCacheTaskTest()
    {
        super( "tasks-snapshot-repo" );
    }

    public void addSnapshotArtifactToProxy( File fileToDeploy )
        throws Exception
    {
        String repositoryUrl = "file://" + localStorageDir + "/tasks-snapshot-repo";
        MavenDeployer.deploy( GAV, repositoryUrl, fileToDeploy, null );
    }

    @Test
    public void clearCacheTask()
        throws Exception
    {
        /*
         * fetch something from a remote repo, run clearCache from root, on _remote repo_ put a newer timestamped file,
         * and rerequest again the same (the filenames will be the same, only the content/timestamp should change),
         * nexus should refetch it. BUT, this works for snapshot nexus reposes only, release reposes do not refetch!
         */
        File artifact1 = getTestFile( "artifact-1.jar" );
        addSnapshotArtifactToProxy( artifact1 );

        File firstDownload = downloadSnapshotArtifact( "tasks-snapshot-repo", GAV, new File( "target/download" ) );
        Assert.assertTrue( "First time, should download artifact 1", // 
                           compareFileSHA1s( firstDownload, artifact1 ) );

        File artifact2 = getTestFile( "artifact-2.jar" );
        addSnapshotArtifactToProxy( artifact2 );
        File secondDownload = downloadSnapshotArtifact( "tasks-snapshot-repo", GAV, new File( "target/download" ) );
        Assert.assertTrue( "Before ClearCache should download artifact 1",// 
                           compareFileSHA1s( secondDownload, artifact1 ) );

        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setId( "repositoryOrGroupId" );
        prop.setValue( "tasks-snapshot-repo" );

        // prop = new ScheduledServicePropertyResource();
        // prop.setId( "resourceStorePath" );
        // prop.setValue( "/" );

        // This is THE important part
        TaskScheduleUtil.runTask( ClearCacheTaskDescriptor.ID, prop );

        File thirdDownload = downloadSnapshotArtifact( "tasks-snapshot-repo", GAV, new File( "target/download" ) );
        Assert.assertTrue( "After ClearCache should download artifact 2", //
                           compareFileSHA1s( thirdDownload, artifact2 ) );
    }

}
