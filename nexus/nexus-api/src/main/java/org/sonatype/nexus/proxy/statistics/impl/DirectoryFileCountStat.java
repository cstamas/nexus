package org.sonatype.nexus.proxy.statistics.impl;

import java.io.File;
import java.io.FileFilter;

import org.sonatype.nexus.proxy.statistics.AbstractSampledStat;
import org.sonatype.nexus.util.FileUtils;

public class DirectoryFileCountStat
    extends AbstractSampledStat<Long>
{
    private final File directory;

    private final FileFilter filter;

    public DirectoryFileCountStat( final String name, final File directory )
    {
        this( name, directory, null );
    }

    public DirectoryFileCountStat( final String name, final File directory, final FileFilter filter )
    {
        super( name, null );

        if ( directory == null )
        {
            throw new IllegalArgumentException( "The directory may not be null!" );
        }

        this.directory = directory;

        this.filter = filter;
    }

    @Override
    protected Long doSample()
    {
        if ( directory.isDirectory() )
        {
            return FileUtils.filesInDirectorySilently( directory, filter );
        }

        return null;
    }
}
