package org.sonatype.nexus.integrationtests.nexus977tasks;

import java.io.File;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.RepairIndexTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.UpdateIndexTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus977GroupOfGroupsReindexTaskIT
    extends AbstractNexusProxyIntegrationTest
{

    @Test
    public void reindex()
        throws Exception
    {
        List<NexusArtifact> result = getSearchMessageUtil().searchForGav( getTestId(), "project", null, "g4" );
        // deployed artifacts get automatically indexed
        Assert.assertEquals( 3, result.size() );

        // add some extra artifacts
        File dest = new File( nexusWorkDir, "storage/r1/nexus977tasks/project/1.0/project-1.0.jar" );
        dest.getParentFile().mkdirs();
        FileUtils.copyFile( getTestFile( "project.jar" ), dest );

        dest = new File( nexusWorkDir, "storage/r2/nexus977tasks/project/2.0/project-2.0.jar" );
        dest.getParentFile().mkdirs();
        FileUtils.copyFile( getTestFile( "project.jar" ), dest );

        dest = new File( nexusWorkDir, "storage/r3/nexus977tasks/project/3.0/project-3.0.jar" );
        dest.getParentFile().mkdirs();
        FileUtils.copyFile( getTestFile( "project.jar" ), dest );

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryOrGroupId" );
        repo.setValue( "group_g4" );
        TaskScheduleUtil.runTask( "ReindexTaskDescriptor-snapshot", RepairIndexTaskDescriptor.ID, repo );
        
        result = getSearchMessageUtil().searchForGav( getTestId(), "project", null, "g4" );
        Assert.assertEquals( 8, result.size() );
    }

}
