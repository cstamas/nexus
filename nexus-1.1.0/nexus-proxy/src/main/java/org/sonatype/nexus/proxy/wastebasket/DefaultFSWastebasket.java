package org.sonatype.nexus.proxy.wastebasket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryType;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

/**
 * A default FS based implementation.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.nexus.proxy.wastebasket.Wastebasket"
 */
public class DefaultFSWastebasket
    implements SmartWastebasket, EventListener, Initializable
{
    /**
     * @plexus.requirement
     */
    private ApplicationConfiguration applicationConfiguration;

    private File wastebasketDirectory;

    private DeleteOperation deleteOperation = DeleteOperation.MOVE_TO_TRASH;

    public void initialize()
        throws InitializationException
    {
        applicationConfiguration.addProximityEventListener( this );
    }

    public void onProximityEvent( AbstractEvent evt )
    {
        if ( ConfigurationChangeEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            wastebasketDirectory = null;
        }
    }

    protected File getWastebasketDirectory()
    {
        if ( wastebasketDirectory == null )
        {
            wastebasketDirectory = applicationConfiguration.getWastebasketDirectory();

            wastebasketDirectory.mkdirs();
        }

        return wastebasketDirectory;
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

    public long getItemCount()
        throws IOException
    {
        return org.sonatype.nexus.util.FileUtils.filesInDirectory( getWastebasketDirectory() );
    }

    public long getSize()
        throws IOException
    {
        return FileUtils.sizeOfDirectory( getWastebasketDirectory() );
    }

    public void purge()
        throws IOException
    {
        FileUtils.cleanDirectory( getWastebasketDirectory() );
    }

    public void delete( RepositoryItemUid uid, LocalRepositoryStorage ls )
        throws StorageException
    {
        try
        {
            if ( DeleteOperation.MOVE_TO_TRASH.equals( getDeleteOperation() ) )
            {
                AbstractStorageItem item = ls.retrieveItem( uid );

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
                    InputStream is = ( (StorageFileItem) item ).getInputStream();

                    FileOutputStream fos = null;

                    try
                    {
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
                        File itemFile = ( (DefaultFSLocalRepositoryStorage) ls ).getFileFromBase( item
                            .getRepositoryItemUid() );

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

            ls.shredItem( uid );
        }
        catch ( ItemNotFoundException e )
        {
            // silent
        }
        catch ( IOException e )
        {
            // yell
            throw new StorageException( "Got IOException during wastebasket handling!", e );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // yell
            throw new StorageException( "Deletion operation is unsupported!", e );
        }
    }

    // ==============================
    // SmartWastebasket iface
    public void setMaxSizeInBytes( long bytes )
    {
        // TODO Auto-generated method stub
    }
    
    
    public void deleteRepositoryFolders( Repository repository )
        throws IOException
    {
        File localStorageFile = new File( new URL( repository.getLocalUrl() ).getFile() );

        File defaultStorageFile = new File(
            new File( applicationConfiguration.getWorkingDirectory(), "storage" ),
            repository.getId() );

        // only remove the storage folder when in default storage case
        if ( defaultStorageFile.getAbsolutePath().equals( localStorageFile.getAbsolutePath() ) )
        {
            delete( defaultStorageFile );
        }

        File indexerFile = null;

        if ( repository.getRepositoryType() == RepositoryType.HOSTED )
        {
            indexerFile = new File( new File( applicationConfiguration.getWorkingDirectory(), "indexer" ), repository
                .getId()
                + "-local" );
        }
        else if ( repository.getRepositoryType() == RepositoryType.PROXY )
        {
            indexerFile = new File( new File( applicationConfiguration.getWorkingDirectory(), "indexer" ), repository
                .getId()
                + "-remote" );
        }

        if ( indexerFile != null )
        {
            delete( indexerFile );
        }

    }
    
    protected void delete( File file )
        throws IOException
    {
        delete( file, "" );
    }

    protected void delete( File file, String base )
        throws IOException
    {
        File basketFile = new File( getWastebasketDirectory(), base + file.getName() );

        if ( file.isDirectory() )
        {
            basketFile.mkdir();

            for ( File child : file.listFiles() )
            {
                delete( child, base + file.getName() + File.separatorChar );
            }
        }
        else if ( file.isFile() )
        {
            moveFileContent( file, basketFile );
        }
        file.delete();
    }

    private void moveFileContent( File from, File to )
        throws IOException
    {

        FileInputStream fis = null;

        FileOutputStream fos = null;

        try
        {

            fis = new FileInputStream( from );

            fos = new FileOutputStream( to );

            IOUtil.copy( fis, fos );

            fos.flush();
        }
        catch ( IOException ioe )
        {
            throw ioe;
        }
        finally
        {
            if ( fis != null )
            {
                IOUtil.close( fis );
            }
            if ( fos != null )
            {
                IOUtil.close( fos );
            }
        }

    }
}
