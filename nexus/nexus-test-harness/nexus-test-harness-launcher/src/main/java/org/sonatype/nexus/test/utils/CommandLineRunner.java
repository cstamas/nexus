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
package org.sonatype.nexus.test.utils;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.StreamPumper;

public class CommandLineRunner
{

    private static final Logger LOG = Logger.getLogger( CommandLineRunner.class );
    private final StringBuffer buffer = new StringBuffer();
    
    
    public int executeAndWait( Commandline cli ) throws CommandLineException, InterruptedException
    {
        Process p = null;
        StreamPumper outPumper = null;
        StreamPumper errPumper = null;

        StreamConsumer out = new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                buffer.append( line ).append( "\n" );
            }
        };

        try
        {
            LOG.debug( "executing: " + cli.toString() );
            p = cli.execute();

            // we really don't need the stream pumps... but just in case... and if your into that whole sys-out style of
            // debugging this is for you...
            outPumper = new StreamPumper( p.getInputStream(), out );
            errPumper = new StreamPumper( p.getErrorStream(), out );

            outPumper.setPriority( Thread.MIN_PRIORITY + 1 );
            errPumper.setPriority( Thread.MIN_PRIORITY + 1 );

            outPumper.start();
            errPumper.start();

            return p.waitFor();
            
        }
        finally
        {
            if ( outPumper != null )
            {
                outPumper.close();
            }

            if ( errPumper != null )
            {
                errPumper.close();
            }
        }
    }
    
    public String getConsoleOutput()
    {
        return this.buffer.toString();
    }
    
}
