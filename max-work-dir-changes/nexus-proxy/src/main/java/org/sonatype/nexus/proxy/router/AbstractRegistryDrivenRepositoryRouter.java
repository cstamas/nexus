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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.RepositoryNotListableException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.target.TargetSet;
import org.sonatype.nexus.proxy.utils.ResourceStoreUtils;

/**
 * The Class AbstractRegistryDrivenRepositoryRouter is the base class for all routers that needs RepositoryRegistry and
 * handles real (non-virtual) and virtual items items. From now on and "below" (subclasses), we are starting to be aware
 * of what and how will we do some store related operations, and implement some basic postprocessing.
 * 
 * @author cstamas
 */
public abstract class AbstractRegistryDrivenRepositoryRouter
    extends AbstractRepositoryRouter
{
    private boolean stopItemSearchOnFirstFoundFile = true;

    /**
     * The repository registry.
     * 
     * @plexus.requirement
     */
    private RepositoryRegistry repositoryRegistry;

    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        super.onConfigurationChange( evt );

        stopItemSearchOnFirstFoundFile = getApplicationConfiguration()
            .getConfiguration().getRouting().getGroups().isStopItemSearchOnFirstFoundFile();
    }

    /**
     * Checks if is stop item search on first file.
     * 
     * @return true, if is stop item search onfirst file
     */
    public boolean isStopItemSearchOnFirstFoundFile()
    {
        return stopItemSearchOnFirstFoundFile;
    }

    /**
     * Gets the repository registry.
     * 
     * @return the repository registry
     */
    public RepositoryRegistry getRepositoryRegistry()
    {
        return repositoryRegistry;
    }

    /**
     * Sets the repository registry.
     * 
     * @param repositoryRegistry the new repository registry
     */
    public void setRepositoryRegistry( RepositoryRegistry repositoryRegistry )
    {
        this.repositoryRegistry = repositoryRegistry;
    }

    // =====================================================================
    // The AbstractRepositoryRouter "contract"

    /**
     * Retrieving an item. We are getting the affected reposes and doing a request against them. Depending on
     * shouldStopOnFirstFile param, we will end up with one or more results (if more reposes processed). Finally, we
     * pass the results to postprocessor.
     */
    protected StorageItem doRetrieveItem( ResourceStoreRequest req )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        List<StorageItem> result = new ArrayList<StorageItem>();

        List<ResourceStore> stores = resolveResourceStoreByRequest( req );

        AccessDeniedException lastAccessDenial = null;

        if ( stores == null )
        {
            // we handle "virtual" paths, not backed by real Reposes since no repos found to serve this request
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Rendering virtual path for " + req.getRequestPath() );
            }
            result = renderVirtualPath( req, false );
        }
        else
        {
            // we handle "real" paths, we have reposes found for it
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Rendering non-virtual path for " + req.getRequestPath() );
            }

            String routerPath = req.getRequestPath();

            req.pushRequestPath( router2substore( req.getRequestPath() ) );

            // going round-robin the gotten reposes in order we got them
            for ( ResourceStore store : stores )
            {
                try
                {
                    StorageItem item = store.retrieveItem( req );

                    ( (AbstractStorageItem) item ).setPath( routerPath );

                    // pop the store also if this is virtual item
                    if ( item.isVirtual() )
                    {
                        ( (AbstractStorageItem) item ).setStore( this );
                    }

                    result.add( item );

                    if ( !StorageCollectionItem.class.isAssignableFrom( item.getClass() )
                        && shouldStopItemSearchOnFirstFoundFile( item ) )
                    {
                        // this is not a coll and we should stop
                        break;
                    }
                }
                catch ( RepositoryNotAvailableException ex )
                {
                    getLogger().info( ex.getMessage() );
                }
                catch ( ItemNotFoundException ex )
                {
                    // silent, we are searching
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "During routed retrieval we had a ItemNotFoundException" + ex.getMessage() );
                    }
                }
                catch ( AccessDeniedException ex )
                {
                    // silent, we are searching but remember it for later
                    lastAccessDenial = ex;

                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "During routed retrieval we had a AccessDenied:", ex );
                    }
                }
                catch ( Exception ex )
                {
                    getLogger().warn(
                        "Got exception during retrieval of " + req.getRequestPath() + " in repositoryId="
                            + store.getId(),
                        ex );
                }
            }

            req.popRequestPath();
        }

        if ( result.size() > 0 )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Found " + result.size() + " items, postprocessing them" );
            }

            StorageItem resItem = retrieveItemPostprocessor( req, result );

            resItem.getItemContext().putAll( req.getRequestContext() );

            return resItem;
        }
        else
        {
            if ( lastAccessDenial != null )
            {
                getLogger().debug( "Banned from all searched repositories, throwing AccessDeniedException." );

                throw lastAccessDenial;
            }
            else
            {
                if ( stores != null && stores.size() == 0 )
                {
                    return new DefaultStorageCollectionItem( this, req.getRequestPath(), true, true );
                }
                else
                {
                    throw new ItemNotFoundException( req.getRequestPath() );
                }
            }
        }
    }

    /**
     * Listing a path. We are getting the affected reposes, doing list() against all of them and finally doing a result
     * merge. Lastly, the postprocessor handles the result.
     */
    protected List<StorageItem> doListItems( ResourceStoreRequest req )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        List<StorageItem> result = new ArrayList<StorageItem>();

        List<ResourceStore> stores = resolveResourceStoreByRequest( req );

        if ( stores == null )
        {
            // we handle "virtual" paths, not backed by real Reposes
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Rendering virtual path for " + req.getRequestPath() );
            }

            result = renderVirtualPath( req, true );

            for ( StorageItem item : result )
            {
                item.getItemContext().putAll( req.getRequestContext() );
            }
        }
        else
        {
            // we handle "real" paths
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Rendering non-virtual path for " + req.getRequestPath() );
            }
            String routerPath = req.getRequestPath().endsWith( RepositoryItemUid.PATH_SEPARATOR ) ? req
                .getRequestPath().substring( 0, req.getRequestPath().length() - 1 ) : req.getRequestPath();

            req.pushRequestPath( router2substore( req.getRequestPath() ) );

            // going store by store
            for ( ResourceStore store : stores )
            {
                try
                {
                    Collection<StorageItem> storeResult = store.list( req );

                    for ( StorageItem item : storeResult )
                    {
                        // fixing paths, now from "substore" namespace to this router namespace
                        ( (AbstractStorageItem) item ).setPath( routerPath + RepositoryItemUid.PATH_SEPARATOR
                            + item.getName() );

                        // pop the store also if this is virtual item
                        if ( item.isVirtual() )
                        {
                            ( (AbstractStorageItem) item ).setStore( this );
                        }

                        // putting context
                        item.getItemContext().putAll( req.getRequestContext() );
                    }

                    // and adding them to total
                    result.addAll( storeResult );
                }
                catch ( RepositoryNotAvailableException e )
                {
                    getLogger().info(
                        "Repository " + store.getId() + " is not available during list of " + req.getRequestPath() );
                }
                catch ( RepositoryNotListableException e )
                {
                    getLogger().info(
                        "Repository " + store.getId() + " is not listable during list of " + req.getRequestPath() );
                }
                catch ( ItemNotFoundException e )
                {
                    // silent, we are merging results
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug(
                            "ItemNotFoundException in repository " + store.getId() + " during list of "
                                + req.getRequestPath() );
                    }
                }
                catch ( AccessDeniedException e )
                {
                    getLogger().info(
                        "Access in repository " + store.getId() + " is denied during list of " + req.getRequestPath() );
                }
            }
            req.popRequestPath();
        }

        return result;
    }

    /**
     * We are copying stuff. We are getting the possible affected source reposes and destination reposes and ensuring we
     * have only one of them.
     */
    protected void doCopyItem( ResourceStoreRequest f, ResourceStoreRequest t )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        List<ResourceStore> sourceList = resolveResourceStoreByRequest( f );

        if ( sourceList.size() != 1 )
        {
            throw new IllegalArgumentException( "You must address one ResourceStore with this operation!" );
        }

        ResourceStore source = sourceList.get( 0 );

        f.pushRequestPath( router2substore( f.getRequestPath() ) );

        List<ResourceStore> destList = resolveResourceStoreByRequest( t );

        if ( destList.size() != 1 )
        {
            throw new IllegalArgumentException( "You must address one ResourceStore with this operation!" );
        }

        ResourceStore destination = destList.get( 0 );

        t.pushRequestPath( router2substore( t.getRequestPath() ) );

        StorageItem item = (AbstractStorageItem) source.retrieveItem( f );

        try
        {
            if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
            {
                destination.storeItem( t, ( (StorageFileItem) item ).getInputStream(), ( (StorageFileItem) item )
                    .getAttributes() );
            }
            else
            {
                throw new UnsupportedStorageOperationException( "The item to be copied is not a file!" );
            }
        }
        catch ( IOException e )
        {
            throw new StorageException( "Store operation failed.", e );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            throw new StorageException( "Store operation not supported.", e );
        }

        f.popRequestPath();

        t.popRequestPath();
    }

    /**
     * A not really proper move implementation: copy + delete.
     */
    protected void doMoveItem( ResourceStoreRequest f, ResourceStoreRequest t )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        doCopyItem( f, t );

        doDeleteItem( f );
    }

    /**
     * Storing an item. Getting the list of affected reposes and ensuring we have only one.
     */
    protected void doStoreItem( ResourceStoreRequest req, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        try
        {
            List<ResourceStore> stores = resolveResourceStoreByRequest( req );

            if ( stores.size() != 1 )
            {
                throw new IllegalArgumentException( "You must address one ResourceStore with this operation!" );
            }

            ResourceStore store = stores.get( 0 );

            req.pushRequestPath( router2substore( req.getRequestPath() ) );

            store.storeItem( req, is, userAttributes );

            req.popRequestPath();
        }
        catch ( ItemNotFoundException e )
        {
            throw new IllegalArgumentException( "You must address one ResourceStore with this operation!", e );
        }
    }

    /**
     * Creating a collection. Getting the list of affected reposes and ensuring we have only one.
     */
    protected void doCreateCollection( ResourceStoreRequest req, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        try
        {
            List<ResourceStore> stores = resolveResourceStoreByRequest( req );

            if ( stores.size() != 1 )
            {
                throw new IllegalArgumentException( "You must address one ResourceStore with this operation!" );
            }

            ResourceStore store = stores.get( 0 );

            req.pushRequestPath( router2substore( req.getRequestPath() ) );

            store.createCollection( req, userAttributes );

            req.popRequestPath();
        }
        catch ( ItemNotFoundException e )
        {
            throw new IllegalArgumentException( "You must address one ResourceStore with this operation!", e );
        }
    }

    /**
     * Deleting and item. We are working only against one targeted repos.
     */
    protected void doDeleteItem( ResourceStoreRequest request )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        List<ResourceStore> stores = resolveResourceStoreByRequest( request );

        if ( stores.size() != 1 )
        {
            throw new IllegalArgumentException( "You must address one ResourceStore with this operation!" );
        }

        ResourceStore repository = stores.get( 0 );

        request.pushRequestPath( router2substore( request.getRequestPath() ) );

        repository.deleteItem( request );

        request.popRequestPath();
    }

    protected TargetSet doGetTargetsForRequest( ResourceStoreRequest request )
    {
        TargetSet result = new TargetSet();

        List<ResourceStore> stores = null;

        try
        {
            stores = resolveResourceStoreByRequest( request );
        }
        catch ( ItemNotFoundException e )
        {
            // thrown when route mapping simply excludes all processable Stores
            // will handle below

            stores = null;
        }
        catch ( NoSuchResourceStoreException e )
        {
            // nothing here
            return result;
        }

        if ( stores == null )
        {
            // we handle "virtual" paths, not backed by real Reposes since no repos found to serve this request
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Request path is not backed by Stores, no targets for: " + request.getRequestPath() );
            }
        }
        else
        {
            // we handle "real" paths, we have reposes found for it
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Request path is backed by " + stores.size() + " Store(s), looking up targets for: "
                        + request.getRequestPath() );
            }

            request.pushRequestPath( router2substore( request.getRequestPath() ) );

            // going round-robin the gotten reposes in order we got them
            for ( ResourceStore store : stores )
            {
                try
                {
                    TargetSet rsTargets = store.getTargetsForRequest( request );

                    result.addTargetSet( rsTargets );
                }
                catch ( Exception ex )
                {
                    getLogger().warn(
                        "Got exception during retrieval of " + request.getRequestPath() + " in repositoryId="
                            + store.getId(),
                        ex );
                }
            }
        }

        return result;
    }

    // =====================================================================
    // Customization stuff No1

    /**
     * Resolve ResourceStore by request. The default behaviour: if we have "targeted" request
     * (request.getRequestRepositoryId() or request.getRequestRepositoryGroupId() is not null), then return those,
     * otherwise do "something" (in subclass).
     * 
     * @param request the request
     * @return the list< repository>
     * @throws NoSuchRepositoryException the no such repository exception
     * @throws NoSuchRepositoryGroupException the no such repository group exception
     */
    protected List<ResourceStore> resolveResourceStoreByRequest( ResourceStoreRequest request )
        throws NoSuchResourceStoreException,
            ItemNotFoundException
    {
        List<ResourceStore> result = null;

        if ( request.getRequestRepositoryId() != null )
        {
            // we have "targeted" request for a repository
            result = new ArrayList<ResourceStore>( 1 );

            result.add( getRepositoryRegistry().getRepository( request.getRequestRepositoryId() ) );
        }
        else if ( request.getRequestRepositoryGroupId() != null )
        {
            // we have "targeted" request for a repo group, get it's members
            List<Repository> group = getRepositoryRegistry().getRepositoryGroup( request.getRequestRepositoryGroupId() );

            result = new ArrayList<ResourceStore>( group.size() );

            result.addAll( group );
        }
        else
        {
            // otherwise it is sublcass dependent how will we get a store
            result = resolveResourceStore( request );
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "ResourceStores involved in servicing request (in processing order): "
                    + ResourceStoreUtils.getResourceStoreListAsString( result ) );
        }

        return result;
    }

    // =====================================================================
    // Customization stuff No1

    /**
     * Simple wrapper, maybe some subclass want's another condition too.
     */
    protected boolean shouldStopItemSearchOnFirstFoundFile( StorageItem item )
    {
        return isStopItemSearchOnFirstFoundFile();
    }

    /**
     * Render virtual path.
     * 
     * @param request the request
     * @param isList is list or just an item requested
     * @return the list< storage item>
     * @throws NoSuchResourceStoreException the no such repository exception
     */
    protected abstract List<StorageItem> renderVirtualPath( ResourceStoreRequest request, boolean isList )
        throws NoSuchResourceStoreException;

    /**
     * Resolve ResourceStore by some means. This is what distinguishes router from router.
     * 
     * @param request the request
     * @return the list of stores
     * @throws NoSuchResourceStoreException the no such repository exception
     */
    protected abstract List<ResourceStore> resolveResourceStore( ResourceStoreRequest request )
        throws NoSuchResourceStoreException;

    /**
     * Router2substore path. It simply mangles the path from this routers "namespace" to the called "substore"
     * namespace. The default implementation simply "strips off" the first path elem. Note: what is first path elem
     * depends on subclasses. Eg. on repoId router it is repository ID, on groupId router it is group ID, etc.
     * 
     * @param routerPath the router path
     * @return the string
     */
    protected String router2substore( String routerPath )
    {
        String path = routerPath.startsWith( RepositoryItemUid.PATH_ROOT ) ? routerPath.substring( 1 ) : routerPath;

        String[] explodedPath = path.split( RepositoryItemUid.PATH_SEPARATOR );

        if ( explodedPath.length < 1 )
        {
            return RepositoryItemUid.PATH_ROOT;
        }

        String result = path.substring( explodedPath[0].length() );

        if ( result.length() == 0 )
        {
            return RepositoryItemUid.PATH_ROOT;
        }
        else
        {
            return result;
        }
    }

}
