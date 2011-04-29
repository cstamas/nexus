package org.sonatype.nexus.proxy.statistics.impl;

import java.util.concurrent.atomic.AtomicLong;

import org.sonatype.nexus.proxy.statistics.AbstractStat;
import org.sonatype.nexus.proxy.statistics.CountingStat;

public class DefaultLongCountingStat
    extends AbstractStat<Long>
    implements CountingStat<Long>
{
    private final AtomicLong value;

    public DefaultLongCountingStat( final String name )
    {
        this( name, 0l );
    }

    public DefaultLongCountingStat( final String name, final long startValue )
    {
        super( name );
        this.value = new AtomicLong( startValue );
    }

    @Override
    public Long value()
    {
        return value.get();
    }

    @Override
    public Long inc()
    {
        return value.incrementAndGet();
    }

    @Override
    public Long inc( final Long val )
    {
        return value.addAndGet( val );
    }
}
