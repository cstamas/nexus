package org.sonatype.nexus.proxy.walker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.ResourceStore;

public class DefaultWalkerContext
    implements WalkerContext
{
    private final ResourceStore resourceStore;

    private final WalkerFilter walkerFilter;

    private final boolean localOnly;

    private final boolean collectionsOnly;

    private Map<String, Object> context;

    private List<WalkerProcessor> processors;

    private Throwable stopCause;

    private volatile boolean running;

    public DefaultWalkerContext( ResourceStore store )
    {
        this( store, null );
    }

    public DefaultWalkerContext( ResourceStore store, WalkerFilter filter )
    {
        this( store, filter, true, false );
    }

    public DefaultWalkerContext( ResourceStore store, WalkerFilter filter, boolean localOnly, boolean collectionsOnly )
    {
        super();

        this.resourceStore = store;

        this.walkerFilter = filter;

        this.localOnly = localOnly;

        this.collectionsOnly = collectionsOnly;

        this.running = true;
    }

    public boolean isLocalOnly()
    {
        return localOnly;
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

    public ResourceStore getResourceStore()
    {
        return resourceStore;
    }

    public boolean isStopped()
    {
        return !running;
    }

    public Throwable getStopCause()
    {
        return stopCause;
    }

    public void stop()
    {
        running = false;
    }

    public void stop( Throwable cause )
    {
        running = false;

        stopCause = cause;
    }

}
