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
package org.sonatype.nexus.maven.tasks.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

@Component( role = ScheduledTaskDescriptor.class, hint = "SnapshotRemoval", description = "Remove Snapshots From Repository" )
public class SnapshotRemovalTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{
    public static final String ID = "SnapshotRemoverTask";

    @Requirement( role = ScheduledTaskPropertyDescriptor.class, hint = "RepositoryOrGroup" )
    private ScheduledTaskPropertyDescriptor repositoryOrGroupId;

    @Requirement( role = ScheduledTaskPropertyDescriptor.class, hint = "MinimumSnapshotCount" )
    private ScheduledTaskPropertyDescriptor minSnapshots;

    @Requirement( role = ScheduledTaskPropertyDescriptor.class, hint = "SnapshotRetentionDays" )
    private ScheduledTaskPropertyDescriptor retentionDays;

    @Requirement( role = ScheduledTaskPropertyDescriptor.class, hint = "RemoveIfReleased" )
    private ScheduledTaskPropertyDescriptor removeWhenReleased;

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Remove Snapshots From Repository";
    }

    public List<ScheduledTaskPropertyDescriptor> getPropertyDescriptors()
    {
        List<ScheduledTaskPropertyDescriptor> properties = new ArrayList<ScheduledTaskPropertyDescriptor>();

        properties.add( repositoryOrGroupId );
        properties.add( minSnapshots );
        properties.add( retentionDays );
        properties.add( removeWhenReleased );

        return properties;
    }
}
