package org.sonatype.nexus.proxy.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

public class Top<V>
    extends AbstractStat<Iterable<V>>
    implements Series<V>
{
    private final Stat<V> source;

    private final TreeSet<V> samples;

    private final int windowSize;

    public Top( final String name, final Stat<V> source, final int windowSize )
    {
        this( name, source, windowSize, null );
    }

    public Top( final String name, final Stat<V> source, final int windowSize, final Comparator<? super V> comparator )
    {
        super( name );

        this.source = source;

        this.windowSize = windowSize;

        this.samples = new TreeSet<V>( comparator );
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

        if ( newSample != null )
        {
            if ( samples.size() == windowSize )
            {
                samples.remove( samples.last() );
            }

            samples.add( newSample );
        }

        return value();
    }
}
