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
package org.sonatype.nexus.proxy;

import org.apache.commons.httpclient.CustomMultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.HttpClient;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.CommonsHttpClientRemoteStorage;

public class SimpleRemoteLeakTest
    extends AbstractProxyTestEnvironment
{

    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );
        return jettyTestsuiteEnvironmentBuilder;
    }

    public void testNothing()
    {
        assertTrue( true );
    }

    public void testSimplerRemoteLeak()
        throws Exception
    {
        // mangle one repos to have quasi different host, thus different HttpCommons HostConfig
        // but make it succeed! (127.0.0.1 is localhost, so will be able to connect)
        getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class )
            .setRemoteUrl(
                           getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class )
                               .getRemoteUrl().replace( "localhost", "127.0.0.1" ) );

        ProxyRepository repo1 = getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class );
        ProxyRepository repo2 = getRepositoryRegistry().getRepositoryWithFacet( "repo2", ProxyRepository.class );

        ResourceStoreRequest req1 =
            new ResourceStoreRequest( "/repositories/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar", false );
        ResourceStoreRequest req2 =
            new ResourceStoreRequest( "/repositories/repo2/xstream/xstream/1.2.2/xstream-1.2.2.pom", false );

        for ( int i = 0; i < 10; i++ )
        {
            StorageItem item1 = getRootRouter().retrieveItem( req1 );
            checkForFileAndMatchContents( item1 );

            StorageItem item2 = getRootRouter().retrieveItem( req2 );
            checkForFileAndMatchContents( item2 );

            // to force refetch
            getRepositoryRegistry().getRepository( item1.getRepositoryId() )
                .deleteItem( false, new ResourceStoreRequest( item1 ) );

            getRepositoryRegistry().getRepository( item2.getRepositoryId() )
                .deleteItem( false, new ResourceStoreRequest( item2 ) );
        }

        // get the default context, since they used it
        RemoteStorageContext ctx1 = repo1.getRemoteStorageContext();

        CustomMultiThreadedHttpConnectionManager cm1 =
            (CustomMultiThreadedHttpConnectionManager) ( (HttpClient) ctx1
                .getContextObject( CommonsHttpClientRemoteStorage.CTX_KEY_CLIENT ) )
                .getHttpConnectionManager();

        assertEquals( 1, cm1.getConnectionsInPool() );

        RemoteStorageContext ctx2 = repo2.getRemoteStorageContext();

        CustomMultiThreadedHttpConnectionManager cm2 =
            (CustomMultiThreadedHttpConnectionManager) ( (HttpClient) ctx2
                .getContextObject( CommonsHttpClientRemoteStorage.CTX_KEY_CLIENT ) )
                .getHttpConnectionManager();

        assertEquals( 1, cm2.getConnectionsInPool() );
    }

    // DISABLED: move to IT, it takes too long (no route to host + java)
    public void nonTestSimplerAvailabilityCheckRemoteLeak()
        throws Exception
    {
        // mangle one repos to have quasi different host, thus different HttpCommons HostConfig
        // but make it fail! (unknown host, so will not be able to connect)
        getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class )
            .setRemoteUrl(
                           getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class )
                               .getRemoteUrl().replace( "localhost", "1.1.1.1" ) );

        ProxyRepository repo1 = getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class );
        ProxyRepository repo2 = getRepositoryRegistry().getRepositoryWithFacet( "repo2", ProxyRepository.class );

        // loop until we have some "sensible" result (not unknown, since this is async op)
        // first unforced request will trigger the check, and wait until we have result
        RemoteStatus rs1 = RemoteStatus.UNKNOWN;
        RemoteStatus rs2 = RemoteStatus.UNKNOWN;

        while ( RemoteStatus.UNKNOWN.equals( rs1 ) || RemoteStatus.UNKNOWN.equals( rs2 ) )
        {
            rs1 = repo1.getRemoteStatus(new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT ), false );
            rs2 = repo2.getRemoteStatus(new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT ), false );

            Thread.sleep( 1000 );
        }

        // get the default context, since they used it
        RemoteStorageContext ctx1 = repo1.getRemoteStorageContext();

        CustomMultiThreadedHttpConnectionManager cm1 =
            (CustomMultiThreadedHttpConnectionManager) ( (HttpClient) ctx1
                .getContextObject( CommonsHttpClientRemoteStorage.CTX_KEY_CLIENT ) )
                .getHttpConnectionManager();

        assertEquals( 1, cm1.getConnectionsInPool() );

        RemoteStorageContext ctx2 = repo2.getRemoteStorageContext();

        CustomMultiThreadedHttpConnectionManager cm2 =
            (CustomMultiThreadedHttpConnectionManager) ( (HttpClient) ctx2
                .getContextObject( CommonsHttpClientRemoteStorage.CTX_KEY_CLIENT ) )
                .getHttpConnectionManager();

        assertEquals( 1, cm2.getConnectionsInPool() );
    }

}
