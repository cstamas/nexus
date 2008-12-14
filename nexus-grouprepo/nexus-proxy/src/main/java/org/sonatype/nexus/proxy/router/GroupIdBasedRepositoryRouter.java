/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.router;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * This is the "old" Proximity bean. It offers the "group view". It expects paths in form "/(groupId)/(repositoryPath)".
 * It handles aggregations within groups if needed.
 * 
 * @author cstamas
 */
public abstract class GroupIdBasedRepositoryRouter
    extends AbstractRegistryDrivenRepositoryRouter
    implements RepositoryRouter
{

    protected List<StorageItem> renderVirtualPath( ResourceStoreRequest request, boolean list )
        throws NoSuchResourceStoreException
    {
        List<StorageItem> result = new ArrayList<StorageItem>();

        if ( list )
        {
            if ( request.getRequestRepositoryGroupId() != null )
            {
                // we have a targeted request at repos
                Repository repository = getRepositoryRegistry().getRepository( request.getRequestRepositoryGroupId() );

                DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( this, RepositoryItemUid.PATH_ROOT
                    + repository.getId(), true, false );

                coll.setRepositoryId( repository.getId() );

                result.add( coll );
            }
            else
            {
                // list all reposes as "root"
                for ( String groupId : getRepositoryRegistry().getRepositoryGroupIds() )
                {
                    DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem(
                        this,
                        RepositoryItemUid.PATH_ROOT + groupId,
                        true,
                        false );

                    result.add( coll );
                }
            }
        }
        else if ( !list )
        {
            DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem(
                this,
                RepositoryItemUid.PATH_ROOT,
                true,
                false );
            result.add( coll );
        }

        return result;
    }

    protected List<ResourceStore> resolveResourceStore( ResourceStoreRequest request )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException
    {
        List<ResourceStore> result = null;

        if ( !RepositoryItemUid.PATH_ROOT.equals( request.getRequestPath() ) )
        {
            String path = request.getRequestPath().startsWith( RepositoryItemUid.PATH_ROOT ) ? request
                .getRequestPath().substring( 1 ) : request.getRequestPath();

            String[] explodedPath = path.split( RepositoryItemUid.PATH_SEPARATOR );

            if ( explodedPath.length >= 1 )
            {
                String groupId = explodedPath[0];

                GroupRepository group = getRepositoryRegistry().getRepositoryGroupXXX( groupId );

                result = new ArrayList<ResourceStore>();

                if ( group.isExposed() )
                {
                    result.add( group );
                }

                // set the groupId in request for later
                request.setRequestRepositoryGroupId( groupId );
            }
        }

        return result;
    }
}
