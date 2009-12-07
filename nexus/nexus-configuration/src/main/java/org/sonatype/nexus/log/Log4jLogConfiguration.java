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
package org.sonatype.nexus.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.util.EnhancedProperties;

/**
 * @author juven
 */
@Component( role = LogConfiguration.class )
public class Log4jLogConfiguration extends AbstractLogEnabled
    implements LogConfiguration<EnhancedProperties>
{
    private static final String NEXUS_REMARK = "Log4j configuration created by Sonatype Nexus";

    @Requirement
    private LogConfigurationSource<File> logConfigurationSource;

    private EnhancedProperties config = new EnhancedProperties();

    public void apply()
    {
        Properties props = new Properties();

        for ( Map.Entry<String, String> e : config.entrySet() )
        {
            props.put( e.getKey(), e.getValue() );
        }

        PropertyConfigurator.configure( props );
    }

    public EnhancedProperties getConfig()
    {
        return config;
    }

    public boolean isUserEdited()
    {
        try
        {
            String configFile = FileUtils.fileRead( logConfigurationSource.getSource() );

            return !configFile.contains( NEXUS_REMARK );
        }
        catch ( IOException e )
        {
            return true;
        }
    }

    public void setConfig( EnhancedProperties config )
    {
        this.config = config;
    }

    public void load()
        throws IOException
    {
        config.clear();

        File logConfigFile = logConfigurationSource.getSource();

        if ( logConfigFile == null )
        {
            getLogger().warn( "No log configuration file found." );

            return;
        }

        FileInputStream inputStream = new FileInputStream( logConfigFile );

        try
        {
            config.load( inputStream, "# " + NEXUS_REMARK );
        }
        finally
        {
            inputStream.close();
        }
    }

    public void save()
        throws IOException
    {
        FileOutputStream outputStream = new FileOutputStream( logConfigurationSource.getSource() );

        try
        {
            config.store( outputStream, NEXUS_REMARK );
        }
        finally
        {
            outputStream.close();
        }
    }

}
