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
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.scheduling.AbstractNexusTask;

/**
 * Publish indexes task.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.scheduling.SchedulerTask"
 *                   role-hint="org.sonatype.nexus.tasks.SynchronizeShadowsTask" instantiation-strategy="per-lookup"
 */
public class SynchronizeShadowsTask
    extends AbstractNexusTask<Object>
{
    private ShadowRepository shadowRepository;

    public ShadowRepository getShadowRepository()
    {
        return shadowRepository;
    }

    public void setShadowRepository( ShadowRepository shadowRepository )
    {
        this.shadowRepository = shadowRepository;
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        getShadowRepository().synchronizeWithMaster();

        return null;
    }

    @Override
    protected String getAction()
    {
        return FeedRecorder.SYSTEM_SYNC_SHADOW_ACTION;
    }

    @Override
    protected String getMessage()
    {
        return "Synchronizing virtual repository '" + getShadowRepository().getName() + "' (ID='"
            + getShadowRepository().getId() + "') with it's master repository '"
            + getShadowRepository().getMasterRepository().getName() + "' (ID='"
            + getShadowRepository().getMasterRepository().getId() + "')";
    }

}
