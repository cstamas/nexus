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
package org.sonatype.nexus.integrationtests.webproxy.nexus1113;

import static org.sonatype.nexus.integrationtests.ITGroups.PROXY;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.codehaus.plexus.util.Base64;
import org.sonatype.nexus.integrationtests.webproxy.AbstractNexusWebProxyIntegrationTest;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProxyWithAuthenticationIT
    extends AbstractNexusWebProxyIntegrationTest
{

    @Override
    @BeforeMethod( alwaysRun = true )
    public void startWebProxy()
        throws Exception
    {
        super.startWebProxy();

        Assert.assertNotNull( server );
        Assert.assertNotNull( server.getProxyServlet() );
        server.getProxyServlet().setUseAuthentication( true );
        server.getProxyServlet().getAuthentications().put( "admin", "123" );
    }

    @Test( groups = PROXY )
    public void validUser()
        throws Exception
    {
        SocketAddress sa = new InetSocketAddress( "127.0.0.1", TestProperties.getInteger( "webproxy.server.port" ) );
        Proxy p = new Proxy( Proxy.Type.HTTP, sa );

        URL url = new URL( "http://www.google.com/index.html" );
        HttpURLConnection con = (HttpURLConnection) url.openConnection( p );

        byte[] encodedUserPwd = Base64.encodeBase64( "admin:123".getBytes() );
        con.setRequestProperty( "Proxy-Authorization", "Basic " + new String( encodedUserPwd ) );
        con.getInputStream();

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

    @Test( groups = PROXY, expectedExceptions = IOException.class )
    public void invalidUser()
        throws Exception
    {
        SocketAddress sa = new InetSocketAddress( "127.0.0.1", TestProperties.getInteger( "webproxy.server.port" ) );
        Proxy p = new Proxy( Proxy.Type.HTTP, sa );

        URL url = new URL( "http://www.google.com/index.html" );
        HttpURLConnection con = (HttpURLConnection) url.openConnection( p );

        byte[] encodedUserPwd = Base64.encodeBase64( "admin:1234".getBytes() );
        con.setRequestProperty( "Proxy-Authorization", "Basic " + new String( encodedUserPwd ) );
        con.getInputStream();

        Assert.fail( "Proxy was not able to access google.com" );
    }

    @Test( groups = PROXY, expectedExceptions = IOException.class )
    public void withoutUser()
        throws Exception
    {
        SocketAddress sa = new InetSocketAddress( "127.0.0.1", TestProperties.getInteger( "webproxy.server.port" ) );
        Proxy p = new Proxy( Proxy.Type.HTTP, sa );

        URL url = new URL( "http://www.google.com/index.html" );
        URLConnection con = url.openConnection( p );

        con.getInputStream();

        Assert.fail( "Proxy was not able to access google.com" );
    }

    @Override
    @AfterMethod( alwaysRun = true )
    public void stopWebProxy()
        throws Exception
    {
        if ( server != null )
        {
            server.getProxyServlet().setUseAuthentication( false );
            server.getProxyServlet().setAuthentications( null );
        }
        super.stopWebProxy();
    }

}
