package org.sonatype.nexus.proxy.statistics;

import java.util.concurrent.LinkedBlockingDeque;

public class Rate<T extends Number>
    extends AbstractSampledStat<Double>
{
    private final Stat<T> stat;

    private final LinkedBlockingDeque<SamplePair> samples;

    public Rate( final String name, final Stat<T> stat, final int windowSize )
    {
        super( name, 0d );

        this.stat = stat;

        this.samples = new LinkedBlockingDeque<Rate.SamplePair>( windowSize );
    }

    @Override
    protected Double doSample()
    {
        T newSample = stat.value();
        long newTimestamp = System.currentTimeMillis();

        double rate = 0;
        if ( !samples.isEmpty() )
        {
            SamplePair oldestSample = samples.peekLast();

            double dy = newSample.doubleValue() - oldestSample.getSample();
            double dt = newTimestamp - oldestSample.getTimestamp();
            rate = dt == 0 ? 0 : ( 1000l * dy ) / dt;
        }

        if ( samples.remainingCapacity() == 0 )
        {
            samples.removeLast();
        }

        samples.addFirst( new SamplePair( newTimestamp, newSample.doubleValue() ) );
        return rate;
    }

    // ==

    private static class SamplePair
    {
        private final long timestamp;

        private final double sample;

        public SamplePair( final long timestamp, final double sample )
        {
            this.timestamp = timestamp;
            this.sample = sample;
        }

        public long getTimestamp()
        {
            return timestamp;
        }

        public double getSample()
        {
            return sample;
        }
    }
}
