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
package org.sonatype.nexus.integrationtests.nexus640;

import java.io.File;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.RebuildAttributesTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the rebuild repository attributes task.
 */
public class Nexus640RebuildRepositoryAttributesTaskIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void rebuildAttributes()
        throws Exception
    {
        String attributePath = "storage/"+REPO_TEST_HARNESS_REPO+"/.nexus/attributes/nexus640/artifact/1.0.0/";

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryOrGroupId" );
        repo.setValue( "repo_" + REPO_TEST_HARNESS_REPO );
        TaskScheduleUtil.runTask( RebuildAttributesTaskDescriptor.ID, repo );

        File jar = new File( nexusWorkDir, attributePath + "artifact-1.0.0.jar" );
        Assert.assertTrue( jar.exists(), "Attribute files should be generated after rebuild" );
        File pom = new File( nexusWorkDir, attributePath + "artifact-1.0.0.pom" );
        Assert.assertTrue( pom.exists(), "Attribute files should be generated after rebuild" );

    }

}
