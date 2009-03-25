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
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.util.EnhancedProperties;

/**
 * Log4J log manager.
 *
 * @author cstamas
 * @author juven
 */
@Component( role = LogManager.class )
public class Log4jLogManager
    extends AbstractLogEnabled
    implements LogManager
{
    @Requirement
    private LogConfiguration<EnhancedProperties> logConfiguration;

    public Log4jLogManager()
    {
        createLogDirectory();
    }

    public File getLogFile( String filename )
    {
        Set<File> logFiles = getLogFiles();

        for ( File logFile : logFiles )
        {
            if ( logFile.getName().equals( filename ) )
            {
                return logFile;
            }
        }

        return null;
    }

    @SuppressWarnings( { "deprecation", "unchecked" } )
    public Set<File> getLogFiles()
    {
        HashSet<File> files = new HashSet<File>();

        files.addAll( getLogFiles( Logger.getRootLogger() ) );

        Enumeration<Category> loggers = Logger.getCurrentCategories();

        while ( loggers.hasMoreElements() )
        {
            Category logger = loggers.nextElement();

            files.addAll( getLogFiles( logger ) );
        }

        return files;
    }

    @SuppressWarnings( "unchecked" )
    protected Set<File> getLogFiles( Category logger )
    {
        HashSet<File> files = new HashSet<File>();

        Enumeration<Appender> appenders = logger.getAllAppenders();

        while ( appenders.hasMoreElements() )
        {
            Appender appender = appenders.nextElement();

            if ( appender instanceof FileAppender )
            {
                String file = ( (FileAppender) appender ).getFile();
                if ( file != null )
                {
                    files.add( new File( file ) );
                }
            }
        }

        return files;
    }

    public void createLogDirectory()
    {
        for ( File file : getLogFiles() )
        {
            File parent = file.getParentFile();

            if ( parent != null && !parent.exists() )
            {
                parent.mkdirs();
            }
        }
    }

    public SimpleLog4jConfig getLogConfig()
        throws IOException
    {
        logConfiguration.load();

        return new SimpleLog4jConfig( logConfiguration.getConfig() );
    }

    public void setLogConfig( SimpleLog4jConfig simpleLog4jConfig )
        throws IOException
    {
        Map<String, String> config = logConfiguration.getConfig();

        config.putAll( simpleLog4jConfig.toMap() );

        logConfiguration.apply();

        logConfiguration.save();
    }

}
