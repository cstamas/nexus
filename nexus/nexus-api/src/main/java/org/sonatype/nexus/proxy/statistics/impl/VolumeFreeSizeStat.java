package org.sonatype.nexus.proxy.statistics.impl;

import java.io.File;

import org.sonatype.nexus.proxy.statistics.AbstractSampledStat;

public class VolumeFreeSizeStat
    extends AbstractSampledStat<Long>
{
    private final File directory;

    public VolumeFreeSizeStat( final String name, final File directory )
    {
        super( name, null );

        if ( directory == null )
        {
            throw new IllegalArgumentException( "The directory may not be null!" );
        }

        this.directory = directory;
    }

    @Override
    protected Long doSample()
    {
        return directory.getFreeSpace();
    }
}
