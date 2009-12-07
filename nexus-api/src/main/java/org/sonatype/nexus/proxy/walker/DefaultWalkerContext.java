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
package org.sonatype.nexus.proxy.walker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;

public class DefaultWalkerContext
    implements WalkerContext
{
    private final Repository resourceStore;

    private final WalkerFilter walkerFilter;

    private final boolean collectionsOnly;

    private final ResourceStoreRequest request;

    private Map<String, Object> context;

    private List<WalkerProcessor> processors;

    private Throwable stopCause;

    private volatile boolean running;

    public DefaultWalkerContext( Repository store, ResourceStoreRequest request )
    {
        this( store, request, null );
    }

    public DefaultWalkerContext( Repository store, ResourceStoreRequest request, WalkerFilter filter )
    {
        this( store, request, filter, true, false );
    }

    public DefaultWalkerContext( Repository store, ResourceStoreRequest request, WalkerFilter filter,
        boolean localOnly, boolean collectionsOnly )
    {
        super();

        this.resourceStore = store;

        this.request = request;

        this.walkerFilter = filter;

        this.collectionsOnly = collectionsOnly;

        this.running = true;
    }

    public boolean isLocalOnly()
    {
        return request.isRequestLocalOnly();
    }

    public boolean isCollectionsOnly()
    {
        return collectionsOnly;
    }

    public Map<String, Object> getContext()
    {
        if ( context == null )
        {
            context = new HashMap<String, Object>();
        }
        return context;
    }

    public List<WalkerProcessor> getProcessors()
    {
        if ( processors == null )
        {
            processors = new ArrayList<WalkerProcessor>();
        }

        return processors;
    }

    public void setProcessors( List<WalkerProcessor> processors )
    {
        this.processors = processors;
    }

    public WalkerFilter getFilter()
    {
        return walkerFilter;
    }

    public Repository getRepository()
    {
        return resourceStore;
    }

    public ResourceStoreRequest getResourceStoreRequest()
    {
        return request;
    }

    public boolean isStopped()
    {
        return !running;
    }

    public Throwable getStopCause()
    {
        return stopCause;
    }

    public void stop( Throwable cause )
    {
        running = false;

        stopCause = cause;
    }

}
