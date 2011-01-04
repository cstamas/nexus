/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus977tasks;

import java.io.File;

import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.EvictUnusedItemsTaskDescriptor;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus977GroupOfGroupsEvictUnusedProxiedItemsTaskIT
    extends AbstractNexusProxyIntegrationTest
{

    @Test
    public void evictUnused()
        throws Exception
    {
        downloadArtifactFromGroup( "g4", GavUtil.newGav( getTestId(), "project", "0.8" ),
                                   "target/downloads/nexus977evict" );
        downloadArtifactFromGroup( "g4", GavUtil.newGav( getTestId(), "project", "2.1" ),
                                   "target/downloads/nexus977evict" );

        Assert.assertTrue( new File( nexusWorkDir, "storage/r4/nexus977tasks/project/0.8/project-0.8.jar" ).exists() );
        Assert.assertTrue( new File( nexusWorkDir, "storage/r5/nexus977tasks/project/2.1/project-2.1.jar" ).exists() );

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryOrGroupId" );
        repo.setValue( "group_g4" );

        ScheduledServicePropertyResource age = new ScheduledServicePropertyResource();
        age.setKey( "evictOlderCacheItemsThen" );
        age.setValue( String.valueOf( 0 ) );

        TaskScheduleUtil.runTask( EvictUnusedItemsTaskDescriptor.ID, repo, age );
        
        Assert.assertFalse( new File( nexusWorkDir, "storage/r4/nexus977tasks/project/0.8/project-0.8.jar" ).exists() );
        Assert.assertFalse( new File( nexusWorkDir, "storage/r5/nexus977tasks/project/2.1/project-2.1.jar" ).exists() );

    }

}
