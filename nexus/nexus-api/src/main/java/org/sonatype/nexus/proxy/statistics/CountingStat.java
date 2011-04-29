package org.sonatype.nexus.proxy.statistics;

public interface CountingStat<T extends Number>
    extends Stat<T>
{
    T inc();

    T inc( T val );
}
