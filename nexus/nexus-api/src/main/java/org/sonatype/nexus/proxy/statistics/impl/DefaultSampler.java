package org.sonatype.nexus.proxy.statistics.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.statistics.SampledStat;
import org.sonatype.nexus.proxy.statistics.Sampler;
import org.sonatype.nexus.threads.NexusThreadFactory;

@Component( role = Sampler.class )
public class DefaultSampler
    implements Sampler
{
    private final ScheduledExecutorService ses;

    private final Map<Frequency, ConcurrentLinkedQueue<SampledStat<?>>> frequencyMap;

    public DefaultSampler()
    {
        this.ses =
            Executors.newScheduledThreadPool( 4, new NexusThreadFactory( "statsampler", "SamplerPool",
                Thread.MIN_PRIORITY, true ) );

        this.frequencyMap = new HashMap<Sampler.Frequency, ConcurrentLinkedQueue<SampledStat<?>>>( 4 );
        this.frequencyMap.put( Frequency.EVERY_SECOND, new ConcurrentLinkedQueue<SampledStat<?>>() );
        this.frequencyMap.put( Frequency.EVERY_MINUTE, new ConcurrentLinkedQueue<SampledStat<?>>() );
        this.frequencyMap.put( Frequency.EVERY_HOUR, new ConcurrentLinkedQueue<SampledStat<?>>() );
        this.frequencyMap.put( Frequency.EVERY_DAY, new ConcurrentLinkedQueue<SampledStat<?>>() );

        ses.scheduleAtFixedRate( new SamplerThread( frequencyMap.get( Frequency.EVERY_SECOND ) ), 0, 1,
            TimeUnit.SECONDS );
        ses.scheduleAtFixedRate( new SamplerThread( frequencyMap.get( Frequency.EVERY_MINUTE ) ), 0, 1,
            TimeUnit.MINUTES );
        ses.scheduleAtFixedRate( new SamplerThread( frequencyMap.get( Frequency.EVERY_HOUR ) ), 0, 1, TimeUnit.HOURS );
        ses.scheduleAtFixedRate( new SamplerThread( frequencyMap.get( Frequency.EVERY_DAY ) ), 0, 1, TimeUnit.DAYS );
    }

    @Override
    public void sample( final SampledStat<?> sampledStat, final Frequency frequency )
    {
        frequencyMap.get( frequency ).add( sampledStat );
    }

    @Override
    public boolean cancel( final SampledStat<?> sampledStat )
    {
        for ( Frequency f : Frequency.values() )
        {
            ConcurrentLinkedQueue<SampledStat<?>> stats = frequencyMap.get( f );

            if ( stats.remove( sampledStat ) )
            {
                return true;
            }
        }

        return false;
    }

    // ==

    private static class SamplerThread
        implements Runnable
    {
        private final Collection<SampledStat<?>> stats;

        public SamplerThread( final Collection<SampledStat<?>> stats )
        {
            this.stats = stats;
        }

        @Override
        public void run()
        {
            for ( SampledStat<?> stat : stats )
            {
                stat.sample();
            }
        }
    }
}
