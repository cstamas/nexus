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
package org.sonatype.nexus.rest.feeds;

import java.util.List;

import org.sonatype.nexus.feeds.NexusArtifactEvent;

/**
 * The overall changes feed.
 * 
 * @author cstamas
 * @plexus.component role-hint="recentChanges"
 */
public class RecentOverallChangesFeedSource
    extends AbstractNexusFeedSource
{
    public static final String CHANNEL_KEY = "recentChanges";

    public String getFeedKey()
    {
        return CHANNEL_KEY;
    }

    public String getFeedName()
    {
        return getDescription();
    }

    @Override
    public String getDescription()
    {
        return "Recent storage changes in all Nexus repositories (caches, deployments, deletions).";
    }

    @Override
    public List<NexusArtifactEvent> getEventList()
    {
        return getNexus().getRecentlyStorageChanges();
    }

    @Override
    public String getTitle()
    {
        return "Recent storage changes";
    }

}
