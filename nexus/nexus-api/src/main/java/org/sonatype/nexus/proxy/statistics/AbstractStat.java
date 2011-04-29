package org.sonatype.nexus.proxy.statistics;

public abstract class AbstractStat<V>
    implements Stat<V>
{
    private final String name;

    public AbstractStat( final String name )
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }
}
