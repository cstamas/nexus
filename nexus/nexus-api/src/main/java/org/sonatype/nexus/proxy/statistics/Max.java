package org.sonatype.nexus.proxy.statistics;

import java.util.Comparator;

public class Max<T>
    extends AbstractSampledStat<T>
{
    private final Stat<T> stat;

    private final Comparator<T> comparator;

    public Max( final String name, final Stat<T> stat )
    {
        this( name, stat, null );
    }

    public Max( final String name, final Stat<T> stat, final Comparator<T> comparator )
    {
        super( name, null );

        this.stat = stat;

        this.comparator = comparator;
    }

    @Override
    protected T doSample()
        throws ClassCastException
    {
        T sample = stat.value();
        T prevSample = value();

        if ( sample == null )
        {
            return prevSample;
        }

        if ( prevSample == null )
        {
            return sample;
        }

        if ( comparator != null )
        {
            if ( comparator.compare( sample, prevSample ) > 0 )
            {
                return sample;
            }
            else
            {
                return prevSample;
            }
        }
        else
        {
            @SuppressWarnings( "unchecked" )
            Comparable<T> cSample = (Comparable<T>) sample;

            if ( cSample.compareTo( prevSample ) > 0 )
            {
                return sample;
            }
            else
            {
                return prevSample;
            }
        }
    }
}
