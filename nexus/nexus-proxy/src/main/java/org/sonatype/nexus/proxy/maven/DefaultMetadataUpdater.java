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
package org.sonatype.nexus.proxy.maven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.IllegalArtifactCoordinateException;
import org.apache.maven.mercury.repository.metadata.AddPluginOperation;
import org.apache.maven.mercury.repository.metadata.AddVersionOperation;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.MetadataOperation;
import org.apache.maven.mercury.repository.metadata.Plugin;
import org.apache.maven.mercury.repository.metadata.PluginOperand;
import org.apache.maven.mercury.repository.metadata.SetSnapshotOperation;
import org.apache.maven.mercury.repository.metadata.SnapshotOperand;
import org.apache.maven.mercury.repository.metadata.StringOperand;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;

@Component( role = MetadataUpdater.class )
public class DefaultMetadataUpdater
    extends AbstractLogEnabled
    implements MetadataUpdater
{
    @Requirement
    private MetadataLocator locator;

    public void deployArtifact( ArtifactStoreRequest request )
        throws IOException, IllegalArtifactCoordinateException
    {
        if ( request.getGav().isHash() || request.getGav().isSignature()
            || StringUtils.isNotBlank( request.getGav().getClassifier() ) )
        {
            // hashes and signatures are "meta"
            // artifacts with classifiers do not change metadata
            return;
        }

        try
        {
            List<MetadataOperation> operations = null;

            Gav gav = locator.getGavForRequest( request );

            // GAV

            Metadata gavMd = locator.retrieveGAVMetadata( request );

            operations = new ArrayList<MetadataOperation>();

            // simply making it foolproof?
            gavMd.setGroupId( gav.getGroupId() );

            gavMd.setArtifactId( gav.getArtifactId() );

            gavMd.setVersion( gav.getBaseVersion() );

            // GAV metadata is only meaningful to snapshot artifacts
            if ( gav.isSnapshot() )
            {
                operations.add( new SetSnapshotOperation( new SnapshotOperand( MetadataBuilder.createSnapshot( request
                    .getVersion() ) ) ) );

                MetadataBuilder.changeMetadata( gavMd, operations );

                locator.storeGAVMetadata( request, gavMd );
            }

            // GA

            operations = new ArrayList<MetadataOperation>();

            Metadata gaMd = locator.retrieveGAMetadata( request );

            operations.add( new AddVersionOperation( new StringOperand( request.getVersion() ) ) );

            MetadataBuilder.changeMetadata( gaMd, operations );

            locator.storeGAMetadata( request, gaMd );

            // G (if is plugin)

            operations = new ArrayList<MetadataOperation>();

            if ( StringUtils.equals( "maven-plugin", locator.retrievePackagingFromPom( request ) ) )
            {
                Metadata gMd = locator.retrieveGMetadata( request );

                Plugin pluginElem = locator.extractPluginElementFromPom( request );

                if ( pluginElem != null )
                {
                    operations.add( new AddPluginOperation( new PluginOperand( pluginElem ) ) );

                    MetadataBuilder.changeMetadata( gMd, operations );

                    locator.storeGMetadata( request, gMd );
                }

            }
        }
        catch ( MetadataException e )
        {
            throw new LocalStorageException( "Not able to apply changes!", e );
        }
    }

    public void undeployArtifact( ArtifactStoreRequest request )
        throws IOException, IllegalArtifactCoordinateException
    {
        if ( request.getGav().isHash() || request.getGav().isSignature()
            || StringUtils.isNotBlank( request.getGav().getClassifier() ) )
        {
            // hashes and signatures are "meta"
            // artifacts with classifiers do not change metadata
            return;
        }

        try
        {
            List<MetadataOperation> operations = null;

            Gav gav = locator.getGavForRequest( request );

            // GAV

            Metadata gavMd = locator.retrieveGAVMetadata( request );

            operations = new ArrayList<MetadataOperation>();

            // simply making it foolproof?
            gavMd.setGroupId( gav.getGroupId() );

            gavMd.setArtifactId( gav.getArtifactId() );

            gavMd.setVersion( gav.getBaseVersion() );

            if ( gav.isSnapshot() )
            {
                operations.add( new SetSnapshotOperation( new SnapshotOperand( MetadataBuilder.createSnapshot( request
                    .getVersion() ) ) ) );
            }

            MetadataBuilder.changeMetadata( gavMd, operations );

            locator.storeGAVMetadata( request, gavMd );

            // GA

            operations = new ArrayList<MetadataOperation>();

            Metadata gaMd = locator.retrieveGAMetadata( request );

            operations.add( new AddVersionOperation( new StringOperand( request.getVersion() ) ) );

            MetadataBuilder.changeMetadata( gaMd, operations );

            locator.storeGAMetadata( request, gaMd );

            // G (if is plugin)

            operations = new ArrayList<MetadataOperation>();

            if ( StringUtils.equals( "maven-plugin", locator.retrievePackagingFromPom( request ) ) )
            {
                Metadata gMd = locator.retrieveGMetadata( request );

                Plugin pluginElem = locator.extractPluginElementFromPom( request );

                if ( pluginElem != null )
                {
                    operations.add( new AddPluginOperation( new PluginOperand( pluginElem ) ) );

                    MetadataBuilder.changeMetadata( gMd, operations );

                    locator.storeGMetadata( request, gMd );
                }

            }
        }
        catch ( MetadataException e )
        {
            throw new LocalStorageException( "Not able to apply changes!", e );
        }
    }

    // ==

    public void deployArtifacts( Collection<ArtifactStoreRequest> requests )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void undeployArtifacts( Collection<ArtifactStoreRequest> requests )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void recreateMetadata( StorageCollectionItem coll )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

}
