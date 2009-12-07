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

import org.sonatype.nexus.proxy.NoSuchRepositoryException;

public interface SnapshotRemover
{
    /**
     * A flag to mark that -- even if we are doing something (eg. deleting, see NEXUS-814) within this GAV -- this GAV
     * is contained multiple times in Repository and this is not the last one, there are more instances (eg. more
     * snapshot builds) for this GAV still left in Repository.
     */
    String MORE_TS_SNAPSHOTS_EXISTS_FOR_GAV = "moreTsSnapshotsExistsForGav";

    SnapshotRemovalResult removeSnapshots( SnapshotRemovalRequest request )
        throws NoSuchRepositoryException, IllegalArgumentException;
}
