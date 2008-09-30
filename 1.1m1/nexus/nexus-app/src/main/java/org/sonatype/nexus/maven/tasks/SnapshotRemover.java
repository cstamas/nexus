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
package org.sonatype.nexus.maven.tasks;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;

public interface SnapshotRemover
{
    String ROLE = SnapshotRemover.class.getName();

    /**
     * A flag to mark that -- even if we are doing something (eg. deleting, see NEXUS-814) within this GAV -- this GAV
     * is contained multiple times in Repository and this is not the last one, there are more instances (eg. more
     * snapshot builds) for this GAV still left in Repository.
     */
    String MORE_TS_SNAPSHOTS_EXISTS_FOR_GAV = "moreTsSnapshotsExistsForGav";

    SnapshotRemovalResult removeSnapshots( SnapshotRemovalRequest request )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            IllegalArgumentException;
}
