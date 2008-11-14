/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.
 *
 * This file is part of Nexus.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */

package org.sonatype.nexus.integrationtests.nexus1022;

import java.io.File;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus1022RebuildRepositoryMavenMetadataTaskTest
    extends AbstractNexusIntegrationTest
{
    @Test
    public void rebuildMavenMetadata()
        throws Exception
    {
/*        if(true) {
            printKnownErrorButDoNotFail( getClass(), "rebuildMavenMetadata" );
            return;
        }*/
        String releaseRepoPath = "storage/nexus-test-harness-repo/";

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();

        repo.setId( "repositoryOrGroupId" );

        repo.setValue( "repo_" + REPO_TEST_HARNESS_REPO );

        ScheduledServiceListResource task = TaskScheduleUtil.runTask("RebuildMavenMetadata-Nexus1022", RebuildMavenMetadataTaskDescriptor.ID, 300, repo );
        Assert.assertNotNull( "The ScheduledServicePropertyResource task didn't run", task );

        File artifactDirMd = new File( nexusWorkDir, releaseRepoPath + "nexus1022/foo/bar/artifact/maven-metadata.xml" );
        Assert.assertTrue( "Maven metadata file should be generated after rebuild", artifactDirMd.exists() );

        File groupPluginMd = new File( nexusWorkDir, releaseRepoPath + "nexus1022/foo/bar/plugins/maven-metadata.xml" );
        Assert.assertTrue( "Maven metadata file should be generated after rebuild", groupPluginMd.exists() );

    }

    @BeforeClass
    public static void cleanWorkFolder() throws Exception {
        cleanWorkDir();
    }

}
