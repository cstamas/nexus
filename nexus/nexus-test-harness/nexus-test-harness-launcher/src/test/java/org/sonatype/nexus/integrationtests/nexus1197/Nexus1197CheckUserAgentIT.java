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
package org.sonatype.nexus.integrationtests.nexus1197;

import java.io.FileNotFoundException;

import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.mortbay.jetty.Server;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.TestProperties;

public class Nexus1197CheckUserAgentIT
    extends AbstractNexusIntegrationTest
{

    private static RequestHandler handler;

    private static Server server;

    public Nexus1197CheckUserAgentIT()
    {
        super( "release-proxy-repo-1" );
    }

    @BeforeClass
    public static void setUp()
        throws Exception
    {
        handler = new RequestHandler();

        server = new Server( TestProperties.getInteger( "proxy.server.port" ) );
        server.setHandler( handler );
        server.start();
    }

    @AfterClass
    public static void tearDown()
        throws Exception
    {
        server.stop();
    }

    @Test
    public void downloadArtifactOverWebProxy()
        throws Exception
    {

        try
        {
            this.downloadArtifact( "nexus1197", "artifact", "1.0", "pom", null, "target/downloads" );
        }
        catch ( FileNotFoundException e )
        {
            // ok, just ignore
        }

        // Nexus/1.2.0-beta-2-SNAPSHOT (OSS; Windows XP; 5.1; x86; 1.6.0_07)
        // apacheHttpClient3x/1.2.0-beta-2-SNAPSHOT Nexus/1.0
        String userAgent = handler.getUserAgent();

        Assert.assertNotNull( userAgent );
        Assert.assertTrue( userAgent.startsWith( "Nexus/" ) );
        Assert.assertThat( userAgent, CoreMatchers.anyOf( StringContains.containsString( "(OSS" ),
                                                          StringContains.containsString( "(PRO" ) ) );

    }

}
