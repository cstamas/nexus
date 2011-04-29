package org.sonatype.nexus.proxy.statistics;

public interface StatisticsSource
{
    Statistics getStatistics();
    
    boolean registerStat( Stat<?> stat );

    boolean registerStat( Stat<?> stat, Class<Stat<?>> key );

    boolean unregisterStat( Class<Stat<?>> s );
}
