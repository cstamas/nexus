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

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;
import org.sonatype.scheduling.SchedulerTask;

/**
 * @author Juven Xu
 */
@Component( role = SchedulerTask.class, hint = RebuildMavenMetadataTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class RebuildMavenMetadataTask
    extends AbstractNexusRepositoriesPathAwareTask<Object>
{

    public static final String REBUILD_MAVEN_METADATA_ACTION = "REBUILD_MAVEN_METADATA";
    
    public Object doRun()
        throws Exception
    {
        if ( getRepositoryGroupId() != null )
        {
            getNexus().rebuildMavenMetadataRepositoryGroup( getResourceStorePath(), getRepositoryGroupId() );
        }
        else if ( getRepositoryId() != null )
        {
            getNexus().rebuildMavenMetadataRepository( getResourceStorePath(), getRepositoryId() );
        }
        else
        {
            getNexus().rebuildMavenMetadataAllRepositories( getResourceStorePath() );
        }

        return null;
    }

    protected String getAction()
    {
        return REBUILD_MAVEN_METADATA_ACTION;
    }

    protected String getMessage()
    {
        if ( getRepositoryGroupId() != null )
        {
            return "Rebuilding maven metadata of repository group with ID=" + getRepositoryGroupId();
        }
        else if ( getRepositoryId() != null )
        {
            return "Rebuilding maven metadata of repository with ID=" + getRepositoryId();
        }
        else
        {
            return "Rebuilding maven metadata of all registered repositories";
        }
    }
}
