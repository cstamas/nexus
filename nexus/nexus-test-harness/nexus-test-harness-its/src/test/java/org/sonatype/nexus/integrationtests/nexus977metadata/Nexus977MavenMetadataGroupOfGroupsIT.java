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
package org.sonatype.nexus.integrationtests.nexus977metadata;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.IOUtil;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionContaining;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataBuilder;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.annotations.Test;

public class Nexus977MavenMetadataGroupOfGroupsIT
    extends AbstractNexusProxyIntegrationTest
{

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryOrGroupId" );
        repo.setValue( "repo_release" );
        TaskScheduleUtil.runTask( "RebuildMavenMetadata-release", RebuildMavenMetadataTaskDescriptor.ID, repo );

        repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryOrGroupId" );
        repo.setValue( "repo_release2" );
        TaskScheduleUtil.runTask( "RebuildMavenMetadata-release2", RebuildMavenMetadataTaskDescriptor.ID, repo );

        repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryOrGroupId" );
        repo.setValue( "repo_snapshot" );
        TaskScheduleUtil.runTask( "RebuildMavenMetadata-snapshot", RebuildMavenMetadataTaskDescriptor.ID, repo );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void checkMetadata()
        throws Exception
    {
        File metadataFile =
            downloadFile( new URL( nexusBaseUrl + "content/repositories/g4/"
                + "nexus977metadata/project/maven-metadata.xml" ), "target/downloads/nexus977" );

        final FileInputStream in = new FileInputStream( metadataFile );
        Metadata metadata = MetadataBuilder.read( in );
        IOUtil.close( in );

        List<String> versions = metadata.getVersioning().getVersions();
        MatcherAssert.assertThat( versions,
                                     IsCollectionContaining.hasItems( "1.5", "1.0.1", "1.0-SNAPSHOT", "0.8", "2.1" ) );
    }
}
