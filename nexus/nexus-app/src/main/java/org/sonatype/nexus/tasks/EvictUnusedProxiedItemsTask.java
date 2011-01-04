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
package org.sonatype.nexus.tasks;

import java.util.Collection;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.nexus.tasks.descriptors.EvictUnusedItemsTaskDescriptor;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Evicts unused proxied items.
 * 
 * @author cstamas
 */
@Component( role = SchedulerTask.class, hint = EvictUnusedItemsTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class EvictUnusedProxiedItemsTask
    extends AbstractNexusRepositoriesTask<Collection<String>>
{
    @Override
    protected String getRepositoryFieldId()
    {
        return EvictUnusedItemsTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
    }

    public int getEvictOlderCacheItemsThen()
    {
        return Integer.parseInt( getParameters().get( EvictUnusedItemsTaskDescriptor.OLDER_THAN_FIELD_ID ) );
    }

    public void setEvictOlderCacheItemsThen( int evictOlderCacheItemsThen )
    {
        getParameters().put( EvictUnusedItemsTaskDescriptor.OLDER_THAN_FIELD_ID,
                             Integer.toString( evictOlderCacheItemsThen ) );
    }

    @Override
    protected Collection<String> doRun()
        throws Exception
    {
        ResourceStoreRequest req = new ResourceStoreRequest( "/" );

        long olderThan = System.currentTimeMillis() - ( getEvictOlderCacheItemsThen() * A_DAY );

        if ( getRepositoryGroupId() != null )
        {
            return getRepositoryRegistry().getRepositoryWithFacet( getRepositoryGroupId(), GroupRepository.class ).evictUnusedItems( req,
                                                                                                                                     olderThan );
        }
        else if ( getRepositoryId() != null )
        {
            return getRepositoryRegistry().getRepositoryWithFacet( getRepositoryId(), Repository.class ).evictUnusedItems( req,
                                                                                                                           olderThan );
        }
        else
        {
            return getNexus().evictAllUnusedProxiedItems( req, olderThan );
        }
    }

    @Override
    protected String getAction()
    {
        return FeedRecorder.SYSTEM_EVICT_UNUSED_PROXIED_ITEMS_ACTION;
    }

    @Override
    protected String getMessage()
    {
        if ( getRepositoryGroupId() != null )
        {
            return "Evicting unused proxied items for repository group " + getRepositoryName() + ".";
        }
        else if ( getRepositoryId() != null )
        {
            return "Evicting unused proxied items for repository " + getRepositoryName() + ".";
        }
        else
        {
            return "Evicting unused proxied items for all registered proxy repositories.";
        }
    }

}
