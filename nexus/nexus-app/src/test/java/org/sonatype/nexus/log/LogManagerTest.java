/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.log;

import java.io.File;
import java.util.Set;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.sonatype.nexus.AbstractNexusTestCase;

/**
 * @author juven
 */
public class LogManagerTest
    extends AbstractNexusTestCase
{
    private LogManager manager;

    @SuppressWarnings( "unused" )
    private org.codehaus.plexus.logging.Logger logger;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        File logFile = new File( getBasedir(), "target/test-classes/log/log-manager-log4j.properties" );

        assertTrue( logFile.exists() );

        System.getProperties().put( "plexus.log4j-prop-file", logFile.getAbsolutePath() );

        manager = lookup( LogManager.class );

        logger = this.getLoggerManager().getLoggerForComponent( LogManagerTest.class.getName() );
    }
    
    @Override
    protected void tearDown()
        throws Exception
    {
        Logger.getLogger( getClass() ).removeAllAppenders();
        super.tearDown();
    }

    public void testLogConfig()
        throws Exception
    {
        SimpleLog4jConfig config = (SimpleLog4jConfig) manager.getLogConfig();

        assertEquals( "DEBUG, console", config.getRootLogger() );

        config.setRootLogger( "INFO, console" );

        manager.setLogConfig( config );

        assertEquals( "INFO, console", ( (SimpleLog4jConfig) manager.getLogConfig() ).getRootLogger() );

        config.setRootLogger( "DEBUG, console" );

        manager.setLogConfig( config );

        assertEquals( "DEBUG, console", ( (SimpleLog4jConfig) manager.getLogConfig() ).getRootLogger() );
    }

    public void testGetLogFiles()
        throws Exception
    {
        Set<File> files = manager.getLogFiles();
        assertTrue( files.isEmpty() );

        // add file appender A to my logger
        Logger testLogger = Logger.getLogger( getClass() );
        File appenderFileA = new File( getPlexusHomeDir(), "logs/appenderA.log" );
        testLogger.addAppender( new FileAppender( new PatternLayout(), appenderFileA.getAbsolutePath() ) );
        testLogger.info( "test log A" );

        files = manager.getLogFiles();
        assertEquals( 1, files.size() );
        assertTrue( files.contains( appenderFileA ) );

        // add file appender B to root logger
        Logger rootLogger = Logger.getRootLogger();
        File appenderFileB = new File( getPlexusHomeDir(), "logs/appenderB.log" );
        rootLogger.addAppender( new FileAppender( new PatternLayout(), appenderFileB.getAbsolutePath() ) );
        rootLogger.info( "test log B" );

        files = manager.getLogFiles();
        assertEquals( 2, files.size() );
        assertTrue( files.contains( appenderFileA ) );
        assertTrue( files.contains( appenderFileB ) );

        // test getLogFile() method
        assertEquals( appenderFileA, manager.getLogFile( "appenderA.log" ) );
        assertEquals( appenderFileB, manager.getLogFile( "appenderB.log" ) );
    }
}
