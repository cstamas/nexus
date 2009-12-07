package org.sonatype.nexus.configuration;

/**
 * Top level interface for wrapping up "external configurations", those used by Repositories and plugins.
 * 
 * @author cstamas
 */
public interface ExternalConfiguration<T>
    extends RevertableConfiguration
{
    T getConfiguration( boolean forModification );
}
