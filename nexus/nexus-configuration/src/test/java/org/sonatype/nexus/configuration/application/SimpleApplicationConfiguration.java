/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.configuration.application;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.ConfigurationCommitEvent;
import org.sonatype.nexus.configuration.ConfigurationPrepareForSaveEvent;
import org.sonatype.nexus.configuration.ConfigurationSaveEvent;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.storage.local.LocalStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

@Component(role=ApplicationConfiguration.class)
public class SimpleApplicationConfiguration
    implements ApplicationConfiguration
{
    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;
    
    private Configuration configuration;

    private LocalStorageContext localStorageContext = new SimpleLocalStorageContext();
    
    private RemoteStorageContext remoteStorageContext = new SimpleRemoteStorageContext();

    public SimpleApplicationConfiguration()
    {
        super();

        this.configuration = new Configuration();

        configuration.setGlobalConnectionSettings( new CRemoteConnectionSettings() );
        // configuration.setGlobalHttpProxySettings( new CRemoteHttpProxySettings() );
        configuration.setRouting( new CRouting() );
        configuration.setRepositoryGrouping( new CRepositoryGrouping() );
    }

    public LocalStorageContext getGlobalLocalStorageContext()
    {
        return localStorageContext;
    }
    
    public RemoteStorageContext getGlobalRemoteStorageContext()
    {
        return remoteStorageContext;
    }

    public Configuration getConfigurationModel()
    {
        return configuration;
    }

    public File getWorkingDirectory()
    {
        return AbstractNexusTestCase.getPlexusHomeDir();
    }

    public File getWorkingDirectory( String key )
    {
        return new File( getWorkingDirectory(), key );
    }

    public File getTemporaryDirectory()
    {
        File dir = getWorkingDirectory( "tmp" );
        dir.mkdirs();
        
        return dir;
    }

    public File getWastebasketDirectory()
    {
        File dir = getWorkingDirectory( "trash" );
        dir.mkdirs();
        
        return dir;
    }

    public File getConfigurationDirectory()
    {
        File dir = new File( getWorkingDirectory(), "conf" );
        dir.mkdirs();
        
        return dir;
    }

    public void saveConfiguration()
        throws IOException
    {
        // send events out, but nothing else
        applicationEventMulticaster.notifyEventListeners( new ConfigurationPrepareForSaveEvent( this ) );
        applicationEventMulticaster.notifyEventListeners( new ConfigurationCommitEvent( this ) );
        applicationEventMulticaster.notifyEventListeners( new ConfigurationSaveEvent( this ) );
    }

    public boolean isSecurityEnabled()
    {
        return false;
    }
}
