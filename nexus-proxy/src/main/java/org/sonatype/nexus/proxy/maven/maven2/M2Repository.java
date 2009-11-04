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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.artifact.M2ArtifactRecognizer;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.IllegalRequestException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ByteArrayContentLocator;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.util.AlphanumComparator;

/**
 * The default M2Repository.
 * 
 * @author cstamas
 */
@Component( role = Repository.class, hint = "maven2", instantiationStrategy = "per-lookup", description = "Maven2 Repository" )
public class M2Repository
    extends AbstractMavenRepository
{
    /**
     * The GAV Calculator.
     */
    @Requirement( hint = "maven2" )
    private GavCalculator gavCalculator;

    @Requirement( hint = "maven2" )
    private ContentClass contentClass;

    @Requirement
    private M2RepositoryConfigurator m2RepositoryConfigurator;

    @Override
    protected M2RepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (M2RepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<M2RepositoryConfiguration>()
        {
            public M2RepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new M2RepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public GavCalculator getGavCalculator()
    {
        return gavCalculator;
    }

    @Override
    protected Configurator getConfigurator()
    {
        return m2RepositoryConfigurator;
    }

    /**
     * Should serve by policies.
     * 
     * @param uid the uid
     * @return true, if successful
     */
    public boolean shouldServeByPolicies( ResourceStoreRequest request )
    {
        if ( M2ArtifactRecognizer.isMetadata( request.getRequestPath() ) )
        {
            if ( M2ArtifactRecognizer.isSnapshot( request.getRequestPath() ) )
            {
                return RepositoryPolicy.SNAPSHOT.equals( getRepositoryPolicy() );
            }
            else
            {
                // metadatas goes always
                return true;
            }
        }
        // we are using Gav to test the path
        Gav gav = null;

        try
        {
            gav = getGavCalculator().pathToGav( request.getRequestPath() );
        }
        catch ( IllegalArtifactCoordinateException e )
        {
            getLogger().info( "Illegal artifact path: '" + request.getRequestPath() + "'" + e.getMessage() );

            return false;
        }

        if ( gav == null )
        {
            return true;
        }
        else
        {
            if ( gav.isSnapshot() )
            {
                // snapshots goes if enabled
                return RepositoryPolicy.SNAPSHOT.equals( getRepositoryPolicy() );
            }
            else
            {
                return RepositoryPolicy.RELEASE.equals( getRepositoryPolicy() );
            }
        }
    }

    public AbstractStorageItem doCacheItem( AbstractStorageItem item )
        throws StorageException
    {
        // if the item is file, is M2 repository metadata and this repo is release-only or snapshot-only
        if ( isCleanseRepositoryMetadata() && StorageFileItem.class.isAssignableFrom( item.getClass() )
            && M2ArtifactRecognizer.isMetadata( item.getPath() ) )
        {
            InputStream orig = null;
            StorageFileItem mdFile = (StorageFileItem) item;
            ByteArrayInputStream backup = null;
            try
            {
                // remote item is not reusable, and we usually cache remote stuff locally
                ByteArrayOutputStream backup1 = new ByteArrayOutputStream();
                orig = mdFile.getInputStream();
                IOUtil.copy( orig, backup1 );
                IOUtil.close( orig );
                backup = new ByteArrayInputStream( backup1.toByteArray() );

                // Metadata is small, let's do it in memory
                MetadataXpp3Reader metadataReader = new MetadataXpp3Reader();
                InputStreamReader isr = new InputStreamReader( backup );
                Metadata imd = metadataReader.read( isr );

                // and fix it
                imd = cleanseMetadataForRepository( RepositoryPolicy.SNAPSHOT.equals( getRepositoryPolicy() ), imd );

                // serialize and swap the new metadata
                MetadataXpp3Writer metadataWriter = new MetadataXpp3Writer();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter( bos );
                metadataWriter.write( osw, imd );
                mdFile.setContentLocator( new ByteArrayContentLocator( bos.toByteArray(), mdFile.getMimeType() ) );
            }
            catch ( Exception e )
            {
                getLogger().error( "Exception during repository metadata cleansing.", e );

                if ( backup != null )
                {
                    // get backup and continue operation
                    backup.reset();
                    mdFile.setContentLocator( new PreparedContentLocator( backup, mdFile.getMimeType() ) );
                }
            }
        }

        return super.doCacheItem( item );
    }

    @Override
    protected boolean isOld( StorageItem item )
    {
        if ( M2ArtifactRecognizer.isMetadata( item.getPath() ) )
        {
            return isOld( getMetadataMaxAge(), item );
        }
        if ( M2ArtifactRecognizer.isSnapshot( item.getPath() ) )
        {
            return isOld( getArtifactMaxAge(), item );
        }

        // we are using Gav to test the path
        Gav gav = null;

        try
        {
            gavCalculator.pathToGav( item.getPath() );
        }
        catch ( IllegalArtifactCoordinateException e )
        {
            getLogger().info( "Illegal artifact path: '" + item.getPath() + "'" + e.getMessage() );
        }

        if ( gav == null )
        {
            // this is not an artifact, it is just any "file"
            return super.isOld( item );
        }
        // it is a release
        return isOld( getArtifactMaxAge(), item );
    }

    @SuppressWarnings( "unchecked" )
    protected Metadata cleanseMetadataForRepository( boolean snapshot, Metadata metadata )
    {
        // remove base versions not belonging here
        List<String> versions = metadata.getVersioning().getVersions();
        for ( Iterator<String> iversion = versions.iterator(); iversion.hasNext(); )
        {
            // if we need snapshots and the version is not snapshot, or
            // if we need releases and the version is snapshot
            if ( ( snapshot && !VersionUtils.isSnapshot( iversion.next() ) )
                || ( !snapshot && VersionUtils.isSnapshot( iversion.next() ) ) )
            {
                iversion.remove();
            }
        }

        metadata.getVersioning().setLatest( getLatestVersion( metadata.getVersioning().getVersions() ) );
        if ( snapshot )
        {
            metadata.getVersioning().setRelease( null );
        }
        else
        {
            metadata.getVersioning().setRelease( metadata.getVersioning().getLatest() );
        }
        return metadata;
    }

    public String getLatestVersion( List<String> versions )
    {
        Collections.sort( versions, new AlphanumComparator() );

        return versions.get( versions.size() - 1 );
    }
    
    @Override
    protected void enforceWritePolicy( ResourceStoreRequest request, Action action )
        throws IllegalRequestException
    {
        // allow updating of metadata
        // we also need to allow updating snapshots
        if( !M2ArtifactRecognizer.isMetadata( request.getRequestPath() ) && 
            !M2ArtifactRecognizer.isSnapshot( request.getRequestPath() ) )
        {
            super.enforceWritePolicy( request, action );
        }
    } 
    
}
