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
package org.sonatype.nexus.configuration.source;

import org.sonatype.configuration.source.ConfigurationSource;
import org.sonatype.nexus.configuration.model.Configuration;

/**
 * The Interface ApplicationConfigurationSource, responsible to fetch Nexus user configuration by some means. It also
 * stores one instance of Configuration object maintained thru life of Nexus. This component is also able to persist
 * user config.
 * 
 * @author cstamas
 */
public interface ApplicationConfigurationSource
    extends ConfigurationSource<Configuration>
{
    /**
     * Returns the configuration that this configuration uses for defaulting.
     * 
     * @return a config source that is default source for this config or null
     */
    ApplicationConfigurationSource getDefaultsSource();
}
