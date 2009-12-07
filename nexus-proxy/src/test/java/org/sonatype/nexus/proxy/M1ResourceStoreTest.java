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

import java.util.Collection;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

public abstract class M1ResourceStoreTest
    extends AbstractProxyTestEnvironment
{

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        
        return new M2TestsuiteEnvironmentBuilder( ss );
    }

    protected abstract ResourceStore getResourceStore()
        throws NoSuchResourceStoreException, Exception;

    protected abstract String getItemPath();

    public void retrieveItem()
        throws Exception
    {
        StorageItem item = getResourceStore().retrieveItem( new ResourceStoreRequest( getItemPath(), false ) );
        checkForFileAndMatchContents( item );
    }

    public void testRetrieveItem()
        throws Exception
    {
        retrieveItem();
    }

    public void testCopyItem()
        throws Exception
    {
        retrieveItem();

        ResourceStoreRequest from = new ResourceStoreRequest( getItemPath(), true );

        ResourceStoreRequest to = new ResourceStoreRequest( getItemPath() + "-copy", true );

        getResourceStore().copyItem( from, to );

        StorageFileItem src = (StorageFileItem) getResourceStore().retrieveItem(
            new ResourceStoreRequest( getItemPath(), true ) );

        StorageFileItem dest = (StorageFileItem) getResourceStore().retrieveItem(
            new ResourceStoreRequest( getItemPath() + "-copy", true ) );

        checkForFileAndMatchContents( src, dest );
    }

    public void testMoveItem()
        throws Exception
    {
        retrieveItem();

        ResourceStoreRequest from = new ResourceStoreRequest( getItemPath(), true );

        ResourceStoreRequest to = new ResourceStoreRequest( getItemPath() + "-copy", true );

        getResourceStore().moveItem( from, to );

        StorageItem item = getResourceStore().retrieveItem( new ResourceStoreRequest( getItemPath() + "-copy", true ) );

        checkForFileAndMatchContents( item, getRemoteFile( getRepositoryRegistry().getRepository(
            "repo1-m1" ), "/activeio/jars/activeio-2.1.jar" ) );

        try
        {
            item = getResourceStore().retrieveItem( new ResourceStoreRequest( getItemPath(), true ) );

            fail();
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }
    }

    public void testDeleteItem()
        throws Exception
    {
        retrieveItem();

        ResourceStoreRequest from = new ResourceStoreRequest( getItemPath(), true );

        getResourceStore().deleteItem( from );

        try
        {
            getResourceStore().retrieveItem( new ResourceStoreRequest( getItemPath(), true ) );

            fail();
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }
    }

    public void testStoreItem()
        throws Exception
    {
        retrieveItem();

        StorageFileItem item = (StorageFileItem) getResourceStore().retrieveItem(
            new ResourceStoreRequest( getItemPath(), true ) );

        ResourceStoreRequest to = new ResourceStoreRequest( getItemPath() + "-copy", true );

        getResourceStore().storeItem( to, item.getInputStream(), null );

        StorageFileItem dest = (StorageFileItem) getResourceStore().retrieveItem(
            new ResourceStoreRequest( getItemPath() + "-copy", true ) );

        checkForFileAndMatchContents( dest, getRemoteFile( getRepositoryRegistry().getRepository(
            "repo1-m1" ), "/activeio/jars/activeio-2.1.jar" ) );

    }

    public void testCreateCollection()
        throws Exception
    {
        retrieveItem();

        ResourceStoreRequest req;
        if ( Repository.class.isAssignableFrom( getResourceStore().getClass() ) )
        {
            req = new ResourceStoreRequest( "/some/path", true );
        }
        else
        {
            req = new ResourceStoreRequest( "/repo1-m1/some/path", true );
        }

        getResourceStore().createCollection( req, null );

        assertTrue( getFile( getRepositoryRegistry().getRepository( "repo1-m1" ), "/some/path" ).exists() );
        assertTrue( getFile( getRepositoryRegistry().getRepository( "repo1-m1" ), "/some/path" ).isDirectory() );
    }

    public void testList()
        throws Exception
    {
        retrieveItem();

        ResourceStoreRequest req = new ResourceStoreRequest( "/", true );

        Collection<StorageItem> res = getResourceStore().list( req );

        for ( StorageItem item : res )
        {
            System.out.println( item.getPath() );
        }
        
        req = new ResourceStoreRequest( "/", true );

        StorageCollectionItem coll = (StorageCollectionItem) getResourceStore().retrieveItem( req );

        for ( StorageItem item : coll.list() )
        {
            System.out.println( item.getPath() );
        }
    }

}
