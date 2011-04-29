package org.sonatype.nexus.proxy.statistics;

public interface SampledStat<V>
    extends Stat<V>
{
    V sample();
}
