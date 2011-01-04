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
package org.sonatype.nexus.proxy.wastebasket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.statistics.DeferredLong;
import org.sonatype.nexus.proxy.statistics.impl.DefaultDeferredLong;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;

/**
 * A default Wastebasket implementation.
 * 
 * @author cstamas
 */
// @Component( role = Wastebasket.class )
public class DefaultFSWastebasket
    implements SmartWastebasket, EventListener, Initializable
{
    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    private File wastebasketDirectory;

    private DeleteOperation deleteOperation = DeleteOperation.MOVE_TO_TRASH;

    public void initialize()
        throws InitializationException
    {
        applicationEventMulticaster.addEventListener( this );
    }

    public void onEvent( Event<?> evt )
    {
        if ( evt instanceof ConfigurationChangeEvent )
        {
            synchronized ( this )
            {
                wastebasketDirectory = null;
            }
        }
    }

    protected File getWastebasketDirectory()
    {
        synchronized ( this )
        {
            if ( wastebasketDirectory == null )
            {
                // wastebasketDirectory = applicationConfiguration.getWastebasketDirectory();

                wastebasketDirectory.mkdirs();
            }

            return wastebasketDirectory;
        }
    }

    protected File getFileForItem( AbstractStorageItem item )
    {
        StringBuffer basketPath = new StringBuffer( item.getRepositoryId() );

        basketPath.append( File.separatorChar );

        basketPath.append( item.getPath() );

        return new File( getWastebasketDirectory(), basketPath.toString() );
    }

    // ==============================
    // Wastebasket iface

    public DeleteOperation getDeleteOperation()
    {
        return deleteOperation;
    }

    public void setDeleteOperation( DeleteOperation deleteOperation )
    {
        this.deleteOperation = deleteOperation;
    }

    public DeferredLong getTotalSize()
    {
        return new DefaultDeferredLong( FileUtils.sizeOfDirectory( getWastebasketDirectory() ) );
    }

    public void purgeAll()
        throws IOException
    {
        FileUtils.cleanDirectory( getWastebasketDirectory() );
    }

    public void purgeAll( long age )
        throws IOException
    {
        removeForever( getWastebasketDirectory(), age );
    }

    public DeferredLong getSize( Repository repository )
    {
        return new DefaultDeferredLong( FileUtils.sizeOfDirectory( new File( getWastebasketDirectory(),
            repository.getId() ) ) );
    }

    public void purge( Repository repository )
        throws IOException
    {
        FileUtils.cleanDirectory( new File( getWastebasketDirectory(), repository.getId() ) );
    }

    public void purge( Repository repository, long age )
        throws IOException
    {
        removeForever( new File( getWastebasketDirectory(), repository.getId() ), age );
    }

    // ==============================
    // SmartWastebasket iface

    public void setMaximumSizeConstraint( MaximumSizeConstraint constraint )
    {
        // TODO: implement this
    }

    // ==============================
    // The rest

    public void delete( LocalRepositoryStorage ls, Repository repository, ResourceStoreRequest request )
        throws LocalStorageException
    {
        try
        {
            if ( DeleteOperation.MOVE_TO_TRASH.equals( getDeleteOperation() ) )
            {
                AbstractStorageItem item = ls.retrieveItem( repository, request );

                // not deleting virtual items
                if ( item.isVirtual() )
                {
                    return;
                }

                File basketFile = getFileForItem( item );

                if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
                {
                    basketFile.getParentFile().mkdirs();

                    // a file or link, it is a simple File
                    InputStream is = null;

                    FileOutputStream fos = null;

                    try
                    {
                        is = ( (StorageFileItem) item ).getInputStream();

                        fos = new FileOutputStream( basketFile );

                        IOUtil.copy( is, fos );

                        fos.flush();
                    }
                    finally
                    {
                        IOUtil.close( is );

                        IOUtil.close( fos );
                    }
                }
                else if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
                {
                    basketFile.mkdirs();

                    // a collection (dir)
                    if ( DefaultFSLocalRepositoryStorage.class.isAssignableFrom( ls.getClass() ) )
                    {
                        // an easy way, we have a File
                        File itemFile = ( (DefaultFSLocalRepositoryStorage) ls ).getFileFromBase( repository, request );

                        FileUtils.copyDirectory( itemFile, basketFile );
                    }
                    else
                    {
                        // a hard way
                        // TODO: walker and recursive deletions?
                        // Currently, we have only the DefaultFSLocalRepositoryStorage implementation :)
                    }
                }
            }

            ls.shredItem( repository, request );
        }
        catch ( ItemNotFoundException e )
        {
            // silent
        }
        catch ( IOException e )
        {
            // yell
            throw new LocalStorageException( "Got IOException during wastebasket handling: " + e.getMessage(), e );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // yell
            throw new LocalStorageException( "Deletion operation is unsupported!", e );
        }
    }

    public boolean undelete( LocalRepositoryStorage ls, Repository repository, ResourceStoreRequest request )
        throws LocalStorageException
    {
        throw new LocalStorageException( "Undelete not supported!" );
    }

    protected void removeForever( File file, long age )
        throws IOException
    {
        if ( file.isFile() )
        {
            if ( isOlderThan( file, age ) )
            {
                FileUtils.forceDelete( file );
            }
        }
        else
        {
            for ( File subFile : file.listFiles() )
            {
                removeForever( subFile, age );
            }
            if ( file.list().length == 0 )
            {
                FileUtils.forceDelete( file );
            }
        }
    }

    /**
     * Move the file to trash, or simply delete it forever
     * 
     * @param file file to be deleted
     * @param deleteForever if it's true, delete the file forever, if it's false, move the file to trash
     * @throws IOException
     */
    protected void delete( File file, boolean deleteForever )
        throws IOException
    {
        if ( !deleteForever )
        {
            File basketFile = new File( getWastebasketDirectory(), file.getName() );

            if ( file.isDirectory() )
            {
                FileUtils.mkdir( basketFile.getAbsolutePath() );

                FileUtils.copyDirectoryStructure( file, basketFile );
            }
            else
            {
                FileUtils.copyFile( file, basketFile );
            }
        }

        FileUtils.forceDelete( file );
    }

    private boolean isOlderThan( File file, long age )
    {
        if ( System.currentTimeMillis() - file.lastModified() > age )
        {
            return true;
        }
        return false;
    }

}
