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
package org.sonatype.nexus.configuration.application.source;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.Configuration;

/**
 * A special "static" configuration source, that always return a factory provided defaults for Nexus configuration. It
 * is unmodifiable, since it actually reads the bundled config file from the module's JAR.
 * 
 * @author cstamas
 */
@Component( role = ApplicationConfigurationSource.class, hint = "static" )
public class StaticConfigurationSource
    extends AbstractApplicationConfigurationSource
{

    /**
     * Gets the configuration using getResourceAsStream from "/META-INF/nexus/nexus.xml".
     */
    public InputStream getConfigurationAsStream()
        throws IOException
    {
        return getClass().getResourceAsStream( "/META-INF/nexus/nexus.xml" );
    }

    public Configuration loadConfiguration()
        throws ConfigurationException,
            IOException
    {
        loadConfiguration( getConfigurationAsStream() );

        return getConfiguration();
    }

    /**
     * This method will always throw UnsupportedOperationException, since NexusDefaultsConfigurationSource is read only.
     */
    public void storeConfiguration()
        throws IOException
    {
        throw new UnsupportedOperationException( "The NexusDefaultsConfigurationSource is static source!" );
    }

    /**
     * Static configuration has no default source, hence it cannot be defalted. Always returns false.
     */
    public boolean isConfigurationDefaulted()
    {
        return false;
    }

}
