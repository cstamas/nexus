/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.nexus.util.ApplicationInterpolatorProvider;

/**
 * The Class EhCacheCacheManager is a thin wrapper around EhCache, just to make things going.
 * 
 * @author cstamas
 * @plexus.component role-hint="default"
 */
public class EhCacheCacheManager
    extends AbstractLogEnabled
    implements CacheManager, Startable
{

    /**
     * The application interpolation service.
     * 
     * @plexus.requirement
     */
    private ApplicationInterpolatorProvider interpolatorProvider;

    /** The eh cache manager. */
    private net.sf.ehcache.CacheManager ehCacheManager;

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.proxy.cache.CacheManager#getCache(java.lang.String)
     */
    public PathCache getPathCache( String cache )
    {
        if ( ehCacheManager == null )
        {
            constructEhCacheManager();
        }

        if ( !ehCacheManager.cacheExists( cache ) )
        {
            ehCacheManager.addCache( cache );
        }

        return new EhCachePathCache( ehCacheManager.getEhcache( cache ) );
    }

    public void start()
        throws StartingException
    {
        constructEhCacheManager();
    }

    public void stop()
        throws StoppingException
    {
        getLogger().info( "Shutting down EHCache manager." );
        ehCacheManager.shutdown();
    }

    private void constructEhCacheManager()
    {
        InputStream configStream = EhCacheCacheManager.class.getResourceAsStream( "/ehcache.xml" );

        if ( configStream != null )
        {
            Configuration ehConfig = ConfigurationFactory.parseConfiguration( configStream );

            configureDiskStore( ehConfig );

            getLogger().info(
                "Creating and configuring EHCache manager with classpath:/ehcache.xml, using disk store '"
                    + ehConfig.getDiskStoreConfiguration().getPath() + "'" );

            ehCacheManager = new net.sf.ehcache.CacheManager( ehConfig );
        }
        else
        {
            configStream = EhCacheCacheManager.class.getResourceAsStream( "/ehcache-default.xml" );

            if ( configStream != null )
            {
                getLogger()
                    .info(
                        "No user EHCache configuration found, creating EHCache manager and configuring it with classpath:/ehcache-default.xml." );

                Configuration ehConfig = ConfigurationFactory.parseConfiguration( configStream );

                configureDiskStore( ehConfig );

                getLogger().info(
                    "Creating and configuring EHCache manager with Nexus Default EHCache Configuration, using disk store '"
                        + ehConfig.getDiskStoreConfiguration().getPath() + "'" );

                ehCacheManager = new net.sf.ehcache.CacheManager( ehConfig );
            }
            else
            {
                getLogger()
                    .warn(
                        "Creating 'default' EHCache manager since no user or default ehcache.xml configuration found on classpath root." );

                ehCacheManager = new net.sf.ehcache.CacheManager();
            }
        }
    }

    private void configureDiskStore( Configuration ehConfig )
    {
        // add plexus awareness with interpolation
        String path = ehConfig.getDiskStoreConfiguration().getPath();

        path = interpolatorProvider.interpolate( path, "" );

        try
        {
            path = new File( path ).getCanonicalPath();
        }
        catch ( IOException e )
        {
            getLogger().warn( "Could not canonize the path '" + path + "'!", e );
        }

        ehConfig.getDiskStoreConfiguration().setPath( path );
    }
}
