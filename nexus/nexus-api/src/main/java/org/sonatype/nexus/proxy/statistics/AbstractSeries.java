package org.sonatype.nexus.proxy.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingDeque;

public class AbstractSeries<V>
    extends AbstractStat<Iterable<V>>
    implements Series<V>
{
    private final Stat<V> source;

    private final LinkedBlockingDeque<V> samples;

    public AbstractSeries( final String name, final Stat<V> source, final int windowSize )
    {
        super( name );

        this.source = source;

        this.samples = new LinkedBlockingDeque<V>( windowSize );
    }

    @Override
    public Iterable<V> value()
    {
        return Collections.unmodifiableCollection( new ArrayList<V>( samples ) );
    }

    @Override
    public Iterable<V> sample()
    {
        V newSample = source.value();

        if ( newSample == null )
        {
            return null;
        }

        if ( samples.remainingCapacity() == 0 )
        {
            samples.removeLast();
        }

        samples.add( newSample );

        return value();
    }
}
