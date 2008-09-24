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
package org.sonatype.nexus.tasks;

import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;

/**
 * Clear caches task.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.scheduling.SchedulerTask" role-hint="ClearCacheTask"
 *                   instantiation-strategy="per-lookup"
 */
public class ClearCacheTask
    extends AbstractNexusRepositoriesPathAwareTask<Object>
{
    
    public static final String HINT = "ClearCacheTask";
    
    public Object doRun()
        throws Exception
    {
        if ( getRepositoryGroupId() != null )
        {
            getNexus().clearRepositoryGroupCaches( getResourceStorePath(), getRepositoryGroupId() );
        }
        else if ( getRepositoryId() != null )
        {
            getNexus().clearRepositoryCaches( getResourceStorePath(), getRepositoryId() );
        }
        else
        {
            getNexus().clearAllCaches( getResourceStorePath() );
        }

        return null;
    }

    protected String getAction()
    {
        return FeedRecorder.SYSTEM_CLEARCACHE_ACTION;
    }

    protected String getMessage()
    {
        if ( getRepositoryGroupId() != null )
        {
            return "Clearing caches for repository group with ID=" + getRepositoryGroupId() + " from path "
                + getResourceStorePath() + " and below.";
        }
        else if ( getRepositoryId() != null )
        {
            return "Clearing caches for repository with ID=" + getRepositoryId() + " from path "
                + getResourceStorePath() + " and below.";
        }
        else
        {
            return "Clearing caches for all registered repositories" + " from path " + getResourceStorePath()
                + " and below.";
        }
    }

}
