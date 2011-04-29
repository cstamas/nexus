package org.sonatype.nexus.proxy.statistics;

public interface Statistics
{
    String getName();
    
    <S extends Stat<?>> S getStat( Class<S> key );

    <S extends Stat<?>> Iterable<S> getAllStats();
}
