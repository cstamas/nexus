package org.sonatype.nexus.integrationtests.nexus642;

import java.io.File;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.SynchronizeShadowTaskDescriptor;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus642SynchShadowTaskTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void synchShadowTest()
        throws Exception
    {
        // create shadow repo 'nexus-shadow-repo'
        RepositoryMessageUtil repoUtil = new RepositoryMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
        String shadowRepoId = "nexus-shadow-repo";
        String taskName = "synchShadowTest";

        RepositoryShadowResource repo = new RepositoryShadowResource();
        repo.setId( shadowRepoId );
        repo.setFormat( "maven1" );
        repo.setName( shadowRepoId );
        repo.setRepoType( "virtual" );
        repo.setShadowOf( this.getTestRepositoryId() );
        repo.setSyncAtStartup( false );
        repoUtil.createRepository( repo );

        // create Sync Repo Task
        // repo: 'nexus-shadow-repo'
        // recurrence: 'manual'
        // run it manually
        ScheduledServiceListResource task = this.executeTask( taskName, repo.getId() );

        // check the status:
        Assert.assertEquals( "SUBMITTED", task.getStatus() );

        // download the file using the shadow repo
        File actualFile = this.downloadFile( new URL( this.getBaseNexusUrl() + "content/repositories/" + shadowRepoId
            + "/" + this.getTestId() + "/jars/artifact-5.4.3.jar" ), "target/downloads/nexus642.jar" );
        File expectedFile = this.getTestResourceAsFile( "projects/artifact/artifact.jar" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( expectedFile, actualFile ) );

    }

    private ScheduledServiceListResource executeTask( String taskName, String shadowRepo )
        throws Exception
    {
        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setId( "sync-repo-props" );
        repo.setValue( shadowRepo );

        ScheduledServicePropertyResource age = new ScheduledServicePropertyResource();
        age.setId( "shadowRepositoryId" );
        age.setValue( shadowRepo );

        // clean unused
        return TaskScheduleUtil.runTask( taskName, SynchronizeShadowTaskDescriptor.ID, repo, age );
    }

}
