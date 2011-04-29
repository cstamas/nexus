package org.sonatype.nexus.proxy.statistics;

import java.util.concurrent.LinkedBlockingDeque;

public class Average<T extends Number>
    extends AbstractSampledStat<Double>
{
    private final Stat<T> stat;

    private final LinkedBlockingDeque<T> samples;

    private double sampleSum = 0;

    public Average( final String name, final Stat<T> stat, final int windowSize )
    {
        super( name, 0d );

        this.stat = stat;

        this.samples = new LinkedBlockingDeque<T>( windowSize );
    }

    @Override
    protected Double doSample()
    {
        T sample = stat.value();

        if ( samples.remainingCapacity() == 0 )
        {
            sampleSum -= samples.removeLast().doubleValue();
        }

        samples.addFirst( sample );
        sampleSum += sample.doubleValue();

        return sampleSum / samples.size();
    }
}
