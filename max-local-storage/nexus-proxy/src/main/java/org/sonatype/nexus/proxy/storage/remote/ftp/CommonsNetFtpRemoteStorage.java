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
package org.sonatype.nexus.proxy.storage.remote.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URL;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.remote.AbstractRemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

/**
 * The CommonsNetFTP remote storage.
 * 
 * @author cstamas
 * @plexus.component role-hint="commonsNetFtp"
 */
public class CommonsNetFtpRemoteStorage
    extends AbstractRemoteRepositoryStorage
    implements RemoteRepositoryStorage
{

    public static final String CTX_KEY = "commonsNetFtp";

    public static final String CTX_KEY_CONFIG = CTX_KEY + ".config";

    public boolean isReachable( Repository repository )
    {
        FTPClient client = null;
        try
        {
            client = getFTPClient( repository );
            try
            {
                // just make request to ping remote
                client.listFiles( "/" );

                return true;
            }
            catch ( IOException e )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Cannot execute FTP operation on remote peer.", e );
                }

                return false;
            }
        }
        catch ( StorageException e )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Cannot create FTP client.", e );
            }

            return false;
        }
        finally
        {
            try
            {
                if ( client != null && client.isConnected() )
                {
                    client.disconnect();
                }
            }
            catch ( IOException ex )
            {
                getLogger().warn( "Could not disconnect FTPClient", ex );
            }
        }
    }

    public boolean containsItem( RepositoryItemUid uid, long newerThan )
        throws StorageException
    {
        FTPClient client = null;
        try
        {
            client = getFTPClient( uid.getRepository() );
            try
            {
                FTPFile[] files = client.listFiles( getHostPath( uid ) );
                return ( files.length == 1 && files[0].isFile() && files[0].getTimestamp().getTimeInMillis() > newerThan );
            }
            catch ( IOException ex )
            {
                throw new StorageException( "Cannot execute FTP operation on remote peer.", ex );
            }
        }
        finally
        {
            try
            {
                if ( client != null && client.isConnected() )
                {
                    client.disconnect();
                }
            }
            catch ( IOException ex )
            {
                getLogger().warn( "Could not disconnect FTPClient", ex );
            }
        }
    }

    public AbstractStorageItem retrieveItem( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException
    {
        return retrieveItem( uid, 0 );
    }

    public AbstractStorageItem retrieveItem( RepositoryItemUid uid, long newerThan )
        throws ItemNotFoundException,
            StorageException
    {
        FTPClient client = null;
        try
        {
            client = getFTPClient( uid.getRepository() );
            try
            {
                FTPFile[] files = client.listFiles( getHostPath( uid ) );
                if ( files.length == 1 && files[0].isFile() && files[0].getTimestamp().getTimeInMillis() > newerThan )
                {
                    AbstractStorageItem fItem = getStorageItemFromFtpFile( uid, files[0], client
                        .retrieveFileStream( getHostPath( uid ) ) );
                    return fItem;
                }
                else
                {
                    throw new ItemNotFoundException( "Item " + uid + " not found in FTP remote peer of "
                        + uid.getRepository().getRemoteUrl() );
                }
            }
            catch ( IOException ex )
            {
                throw new StorageException( "Cannot execute FTP operation on remote peer.", ex );
            }
        }
        finally
        {
            try
            {
                if ( client != null && client.isConnected() )
                {
                    client.disconnect();
                }
            }
            catch ( IOException ex )
            {
                getLogger().warn( "Could not disconnect FTPClient", ex );
            }
        }
    }

    public void storeItem( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            StorageException
    {
        // TODO: implement this
        throw new UnsupportedStorageOperationException( "This operation is not supported on " + this.getClass() );
    }

    public void deleteItem( RepositoryItemUid uid )
        throws ItemNotFoundException,
            UnsupportedStorageOperationException,
            StorageException
    {
        // TODO: implement this
        throw new UnsupportedStorageOperationException( "This operation is not supported on " + this.getClass() );
    }

    // =============================================================
    // inner stuff

    protected void updateContext( RemoteStorageContext ctx )
    {
        FTPClientConfig ftpClientConfig = (FTPClientConfig) ctx.getRemoteConnectionContext().get( CTX_KEY_CONFIG );

        if ( ftpClientConfig == null )
        {
            ftpClientConfig = new FTPClientConfig( FTPClientConfig.SYST_UNIX );
        }

        ctx.getRemoteConnectionContext().put( CTX_KEY_CONFIG, ftpClientConfig );
    }

    protected String getHostPath( RepositoryItemUid uid )
        throws StorageException
    {
        URL remoteUrl = getAbsoluteUrlFromBase( uid );
        return remoteUrl.getPath();
    }

    protected FTPClient getFTPClient( Repository repository )
        throws StorageException
    {
        RemoteStorageContext ctx = getRemoteStorageContext( repository );

        FTPClientConfig ftpClientConfig = (FTPClientConfig) ctx.getRemoteConnectionContext().get( CTX_KEY_CONFIG );

        FTPClient ftpc = null;
        try
        {
            URL remoteUrl = new URL( repository.getRemoteUrl() );
            getLogger().info( "Creating CommonsNetFTPClient instance" );
            ftpc = new FTPClient();
            ftpc.configure( ftpClientConfig );
            ftpc.connect( remoteUrl.getHost() );
            int reply = ftpc.getReplyCode();
            if ( !FTPReply.isPositiveCompletion( reply ) )
            {
                throw new StorageException( "FTP Server refused connection, reply code = " + reply );
            }
            if ( ftpc.login( ctx.getRemoteAuthenticationSettings().getUsername(), ctx
                .getRemoteAuthenticationSettings().getPassword() ) )
            {
                ftpc.setFileType( FTPClient.BINARY_FILE_TYPE );
                return ftpc;
            }
            else
            {
                throw new StorageException( "Could not login, are credentials OK?" );
            }
        }
        catch ( SocketException ex )
        {
            throw new StorageException( "Got SocketException while creating FTPClient", ex );
        }
        catch ( IOException ex )
        {
            throw new StorageException( "Got IOException while creating FTPClient", ex );
        }
    }

    protected AbstractStorageItem getStorageItemFromFtpFile( RepositoryItemUid uid, FTPFile file, InputStream is )
    {
        DefaultStorageFileItem fItem = new DefaultStorageFileItem( uid.getRepository(), uid.getPath(), true, true, is );
        fItem.setLength( file.getSize() );
        fItem.setModified( file.getTimestamp().getTimeInMillis() );
        fItem.setCreated( fItem.getModified() );
        return fItem;
    }

}
