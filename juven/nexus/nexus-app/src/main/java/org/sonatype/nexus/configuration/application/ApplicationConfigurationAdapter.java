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
package org.sonatype.nexus.configuration.application;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;

/**
 * Adapter for NexusConfiguration.
 * 
 * @author cstamas
 */
@Component( role = ApplicationConfiguration.class )
public class ApplicationConfigurationAdapter
    implements ApplicationConfiguration
{
    @Requirement
    private NexusConfiguration nexusConfiguration;

    public Configuration getConfiguration()
    {
        return nexusConfiguration.getConfiguration();
    }

    public File getWorkingDirectory()
    {
        return nexusConfiguration.getWorkingDirectory();
    }

    public File getWorkingDirectory( String key )
    {
        return nexusConfiguration.getWorkingDirectory( key );
    }

    public File getTemporaryDirectory()
    {
        return nexusConfiguration.getTemporaryDirectory();
    }

    public File getWastebasketDirectory()
    {
        return nexusConfiguration.getWastebasketDirectory();
    }

    public File getConfigurationDirectory()
    {
        return nexusConfiguration.getConfigurationDirectory();
    }

    public void saveConfiguration()
        throws IOException
    {
        nexusConfiguration.saveConfiguration();
    }

    public void addProximityEventListener( EventListener listener )
    {
        nexusConfiguration.addProximityEventListener( listener );
    }

    public void removeProximityEventListener( EventListener listener )
    {
        nexusConfiguration.removeProximityEventListener( listener );
    }

    public void notifyProximityEventListeners( AbstractEvent evt )
    {
        nexusConfiguration.notifyProximityEventListeners( evt );
    }

    public boolean isSecurityEnabled()
    {
        return nexusConfiguration.isSecurityEnabled();
    }

}
