package org.sonatype.nexus.proxy.statistics;

public interface Stat<V>
{
    String getName();
    
    V value();
}
