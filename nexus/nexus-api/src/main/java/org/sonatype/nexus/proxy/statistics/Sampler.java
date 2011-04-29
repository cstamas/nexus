package org.sonatype.nexus.proxy.statistics;

public interface Sampler
{
    public enum Frequency
    {
        EVERY_SECOND, EVERY_MINUTE, EVERY_HOUR, EVERY_DAY;
    };

    void sample( SampledStat<?> sampledStat, Frequency frequency );

    boolean cancel( SampledStat<?> sampledStat );
}
