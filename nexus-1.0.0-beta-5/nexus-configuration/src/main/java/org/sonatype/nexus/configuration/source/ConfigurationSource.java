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
package org.sonatype.nexus.configuration.source;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.validator.ValidationResponse;

/**
 * The Interface ConfigurationSource, responsible to fetch Nexus user configuration by some means. It also stores one
 * instance of Configuration object maintained thru life of Nexus. This component is also able to persist user config.
 * 
 * @author cstamas
 */
public interface ConfigurationSource
{

    String ROLE = ConfigurationSource.class.getName();

    /**
     * Gets the current configuration.
     * 
     * @return the configuration, null if not loaded
     * @throws ConfigurationException
     * @throws IOException
     */
    Configuration getConfiguration();

    /**
     * Returns the validation response, if any. It is created on the loading of the user configuration.
     * 
     * @return the response or null if not applicable or config was still not loaded.
     */
    ValidationResponse getValidationResponse();

    /**
     * Forces reloading the user configuration.
     * 
     * @return the configuration
     * @throws ConfigurationException
     * @throws IOException
     */
    Configuration loadConfiguration()
        throws ConfigurationException,
            IOException;

    /**
     * Persists the current configuration.
     * 
     * @throws IOException
     */
    void storeConfiguration()
        throws IOException;

    /**
     * Returns the actual content of user configuration as stream.
     * 
     * @return
     * @throws IOException
     */
    InputStream getConfigurationAsStream()
        throws IOException;

    /**
     * Returns whether the configuration was upgraded.
     * 
     * @return true if the user configuration was upgraded, false otherwise
     */
    boolean isConfigurationUpgraded();

    /**
     * Returns true if the configuration was got from defaults.
     * 
     * @return
     */
    boolean isConfigurationDefaulted();

    /**
     * Returns the configuration that this configuration uses for defaulting.
     * 
     * @return a config source that is default source for this config or null
     */
    ConfigurationSource getDefaultsSource();

}
