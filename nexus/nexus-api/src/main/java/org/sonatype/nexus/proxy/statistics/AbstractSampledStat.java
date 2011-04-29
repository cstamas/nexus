package org.sonatype.nexus.proxy.statistics;

public abstract class AbstractSampledStat<V>
    extends AbstractStat<V>
    implements SampledStat<V>
{
    private V previous;

    public AbstractSampledStat( final String name, final V value )
    {
        super( name );
        this.previous = value;
    }

    @Override
    public V value()
    {
        return previous;
    }

    @Override
    public V sample()
    {
        previous = doSample();

        return previous;
    }

    // ==

    protected abstract V doSample();
}
