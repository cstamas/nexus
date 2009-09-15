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
package org.sonatype.nexus.proxy.maven.maven2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.MetadataOperand;
import org.apache.maven.mercury.repository.metadata.MetadataOperation;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.M2ArtifactRecognizer;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.AbstractMavenGroupRepository;
import org.sonatype.nexus.proxy.maven.metadata.mercury.MergeOperation;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

@Component( role = GroupRepository.class, hint = "maven2", instantiationStrategy = "per-lookup", description = "Maven2 Repository Group" )
public class M2GroupRepository
    extends AbstractMavenGroupRepository
{
    /**
     * The GAV Calculator.
     */
    @Requirement( hint = "maven2" )
    private GavCalculator gavCalculator;

    /**
     * Content class.
     */
    @Requirement( hint = "maven2" )
    private ContentClass contentClass;

    private boolean mergeMetadata = true;

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public GavCalculator getGavCalculator()
    {
        return gavCalculator;
    }

    @Override
    protected StorageItem doRetrieveItem( RepositoryItemUid uid, Map<String, Object> context )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        if ( M2ArtifactRecognizer.isMetadata( uid.getPath() ) && !M2ArtifactRecognizer.isChecksum( uid.getPath() ) )
        {
            // metadata checksum files are calculated and cached as side-effect
            // of doRetrieveMetadata.

            try
            {
                return doRetrieveMetadata( uid, context );
            }
            catch ( UnsupportedStorageOperationException e )
            {
                throw new StorageException( e );
            }
        }

        return super.doRetrieveItem( uid, context );
    }

    /**
     * Parse a maven Metadata object from a storage file item
     */
    private Metadata parseMetadata( StorageFileItem fileItem )
        throws IOException, MetadataException
    {
        InputStream inputStream = null;

        try
        {
            inputStream = fileItem.getInputStream();

            return MetadataBuilder.read( inputStream );
        }
        finally
        {
            try
            {
                if ( inputStream != null )
                {
                    inputStream.close();
                }
            }
            catch ( Exception e )
            {
            }
        }
    }

    /**
     * Aggregates metadata from all member repositories
     */
    private StorageItem doRetrieveMetadata( RepositoryItemUid uid, Map<String, Object> context )
        throws StorageException, IllegalOperationException, UnsupportedStorageOperationException, ItemNotFoundException
    {
        List<StorageItem> items = doRetrieveItems( uid, context );

        if ( items.isEmpty() )
        {
            throw new ItemNotFoundException( uid );
        }

        if ( !mergeMetadata )
        {
            // not merging: return the 1st and ciao
            return items.get( 0 );
        }

        List<Metadata> existingMetadatas = new ArrayList<Metadata>();

        try
        {
            for ( StorageItem item : items )
            {
                if ( !( item instanceof StorageFileItem ) )
                {
                    break;
                }

                StorageFileItem fileItem = (StorageFileItem) item;

                try
                {
                    existingMetadatas.add( parseMetadata( fileItem ) );
                }
                catch ( IOException e )
                {
                    getLogger().warn(
                        "IOException during parse of metadata UID=\"" + fileItem.getRepositoryItemUid().toString()
                            + "\", will be skipped from aggregation!", e );
                }
                catch ( MetadataException e )
                {
                    getLogger().warn(
                        "Metadata exception during parse of metadata from UID=\""
                            + fileItem.getRepositoryItemUid().toString() + "\", will be skipped from aggregation!", e );
                }
            }

            if ( existingMetadatas.isEmpty() )
            {
                throw new ItemNotFoundException( uid );
            }

            Metadata result = new Metadata();

            List<MetadataOperation> ops = new ArrayList<MetadataOperation>();

            for ( Metadata metadata : existingMetadatas )
            {
                ops.add( new MergeOperation( new MetadataOperand( metadata ) ) );
            }

            MetadataBuilder.changeMetadata( result, ops );

            // build the result item
            ByteArrayOutputStream resultOutputStream = new ByteArrayOutputStream();

            MetadataBuilder.write( result, resultOutputStream );

            AbstractStorageItem item = createStorageItem( uid, resultOutputStream.toByteArray(), context );

            // build checksum files
            MessageDigest md5Digest = MessageDigest.getInstance( "md5" );

            MessageDigest sha1Digest = MessageDigest.getInstance( "sha1" );

            md5Digest.update( resultOutputStream.toByteArray() );

            sha1Digest.update( resultOutputStream.toByteArray() );

            storeDigest( uid, md5Digest, context );

            storeDigest( uid, sha1Digest, context );

            resultOutputStream.close();

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Item for path " + uid.getPath() + " merged from " + Integer.toString( items.size() )
                        + " found items." );
            }

            item.getItemContext().put( CTX_TRANSITIVE_ITEM, Boolean.TRUE );

            return item;

        }
        catch ( IOException e )
        {
            throw new StorageException( "Got IOException during M2 metadata merging.", e );
        }
        catch ( MetadataException e )
        {
            throw new StorageException( "Got MetadataException during M2 metadata merging.", e );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new StorageException( "Got NoSuchAlgorithmException during M2 metadata merging.", e );
        }
    }

    protected void storeDigest( RepositoryItemUid uid, MessageDigest digest, Map<String, Object> context )
        throws IOException, UnsupportedStorageOperationException, IllegalOperationException
    {
        byte[] bytes = ( new String( Hex.encodeHex( digest.digest() ) ) + "\n" ).getBytes();

        RepositoryItemUid csuid = createUid( uid.getPath() + "." + digest.getAlgorithm().toLowerCase() );

        AbstractStorageItem item = createStorageItem( csuid, bytes, context );

        storeItem( item );
    }

    public boolean isMergeMetadata()
    {
        return mergeMetadata;
    }

    public void setMergeMetadata( boolean mergeMetadata )
    {
        this.mergeMetadata = mergeMetadata;
    }

    @Override
    public void onProximityEvent( AbstractEvent evt )
    {
        super.onProximityEvent( evt );

        if ( evt instanceof ConfigurationChangeEvent )
        {
            ApplicationConfiguration cfg =
                (ApplicationConfiguration) ( (ConfigurationChangeEvent) evt ).getNotifiableConfiguration();

            mergeMetadata = cfg.getConfiguration().getRouting().getGroups().isMergeMetadata();
        }
    }

}
