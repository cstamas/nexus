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
package org.sonatype.nexus.scheduling;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.scheduling.ScheduledTask;

public abstract class AbstractNexusRepositoriesTask<T>
    extends AbstractNexusTask<T>
{
    public static final String REPOSITORY_OR_GROUP_ID_KEY = "repositoryOrGroupId";

    private static final String REPO_PREFIX = "repo_";

    private static final String GROUP_PREFIX = "group_";

    public String getRepositoryId()
    {
        String param = getParameters().get( REPOSITORY_OR_GROUP_ID_KEY );
        if ( param != null && param.startsWith( REPO_PREFIX ) )
        {
            return param.substring( REPO_PREFIX.length() );
        }

        return null;
    }

    public void setRepositoryId( String repositoryId )
    {
        if ( !StringUtils.isEmpty( repositoryId ) )
        {
            getParameters().put( REPOSITORY_OR_GROUP_ID_KEY, REPO_PREFIX + repositoryId );
        }
    }

    public String getRepositoryGroupId()
    {
        String param = getParameters().get( REPOSITORY_OR_GROUP_ID_KEY );
        if ( param != null && param.startsWith( GROUP_PREFIX ) )
        {
            return param.substring( GROUP_PREFIX.length() );
        }

        return null;
    }

    public void setRepositoryGroupId( String repositoryGroupId )
    {
        if ( !StringUtils.isEmpty( repositoryGroupId ) )
        {
            getParameters().put( REPOSITORY_OR_GROUP_ID_KEY, GROUP_PREFIX + repositoryGroupId );
        }
    }

    protected boolean repositorySetIntersectionIsNotEmpty( AbstractNexusRepositoriesTask<?> otherTask )
    {
        return false;
    }

    protected boolean taskOfThisTypeWithIntersectionExists( Map<Class<?>, List<ScheduledTask<?>>> activeTasks )
    {
        List<ScheduledTask<?>> tasksOfThisType = activeTasks.get( this.getClass() );

        if ( tasksOfThisType != null )
        {
            for ( ScheduledTask<?> task : tasksOfThisType )
            {
                // this is same class as we are, it is safe to cast
                if ( repositorySetIntersectionIsNotEmpty( (AbstractNexusRepositoriesTask<?>) task ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean abstractNexusRepositoriesTaskWithIntersectionExists(
        Map<Class<?>, List<ScheduledTask<?>>> activeTasks )
    {
        return false;
    }
}
