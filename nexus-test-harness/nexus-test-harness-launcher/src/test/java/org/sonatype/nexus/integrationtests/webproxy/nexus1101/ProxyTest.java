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
package org.sonatype.nexus.integrationtests.webproxy.nexus1101;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.webproxy.AbstractNexusWebProxyIntegrationTest;
import org.sonatype.nexus.test.utils.TestProperties;

public class ProxyTest
    extends AbstractNexusWebProxyIntegrationTest
{
    @Test
    public void checkWebProxy()
        throws Exception
    {
        SocketAddress sa = new InetSocketAddress( "127.0.0.1", TestProperties.getInteger( "webproxy.server.port" ) );
        Proxy p = new Proxy( Proxy.Type.HTTP, sa );

        URL url = new URL( "http://www.google.com/index.html" );
        URLConnection conn = url.openConnection( p );
        conn.getInputStream();

        for ( int i = 0; i < 100; i++ )
        {
            Thread.sleep( 200 );

            List<String> uris = server.getAccessedUris();
            for ( String uri : uris )
            {
                if ( uri.contains( "google.com" ) )
                {
                    return;
                }
            }
        }

        Assert.fail( "Proxy was not able to access google.com" );
    }
}
