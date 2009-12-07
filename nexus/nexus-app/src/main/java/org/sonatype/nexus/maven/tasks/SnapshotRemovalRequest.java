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
package org.sonatype.nexus.maven.tasks;

import java.util.HashSet;
import java.util.Set;

public class SnapshotRemovalRequest
{
    private final String repositoryId, repositoryGroupId;

    private final int minCountOfSnapshotsToKeep;

    private final int removeSnapshotsOlderThanDays;

    private final boolean removeIfReleaseExists;

    private final Set<String> metadataRebuildPaths;

    public SnapshotRemovalRequest( String repositoryId, String repositoryGroupId, int minCountOfSnapshotsToKeep,
        int removeSnapshotsOlderThanDays, boolean removeIfReleaseExists )
    {
        super();

        this.repositoryId = repositoryId;

        this.repositoryGroupId = repositoryGroupId;

        this.minCountOfSnapshotsToKeep = minCountOfSnapshotsToKeep;

        this.removeSnapshotsOlderThanDays = removeSnapshotsOlderThanDays;

        this.removeIfReleaseExists = removeIfReleaseExists;

        this.metadataRebuildPaths = new HashSet<String>();
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public String getRepositoryGroupId()
    {
        return repositoryGroupId;
    }

    public int getMinCountOfSnapshotsToKeep()
    {
        return minCountOfSnapshotsToKeep;
    }

    public int getRemoveSnapshotsOlderThanDays()
    {
        return removeSnapshotsOlderThanDays;
    }

    public boolean isRemoveIfReleaseExists()
    {
        return removeIfReleaseExists;
    }

    public Set<String> getMetadataRebuildPaths()
    {
        return metadataRebuildPaths;
    }
}
