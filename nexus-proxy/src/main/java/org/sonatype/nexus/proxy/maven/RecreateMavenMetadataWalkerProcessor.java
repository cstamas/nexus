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

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.metadata.AbstractMetadataHelper;
import org.sonatype.nexus.proxy.maven.metadata.DefaultMetadataHelper;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;

/**
 * @author Juven Xu
 */
public class RecreateMavenMetadataWalkerProcessor
    extends AbstractWalkerProcessor
{
    private boolean isHostedRepo;

    private MavenRepository repository;

    private AbstractMetadataHelper mdHelper;

    private Logger logger;

    public RecreateMavenMetadataWalkerProcessor( Logger logger )
    {
        this.logger = logger;
    }

    @Override
    public void beforeWalk( WalkerContext context )
        throws Exception
    {
        isHostedRepo = false;

        repository = context.getRepository() instanceof MavenRepository
            ? (MavenRepository) context.getRepository()
            : null;

        if ( repository != null )
        {
            mdHelper = new DefaultMetadataHelper( logger, repository );

            isHostedRepo = repository.getRepositoryKind().isFacetAvailable( HostedRepository.class );
        }

        setActive( isHostedRepo );
    }

    @Override
    public void onCollectionEnter( WalkerContext context, StorageCollectionItem coll )
    {
        try
        {
            mdHelper.onDirEnter( coll.getPath() );
        }
        catch ( Exception e )
        {
            logger.warn( "Error occured while entering collection '" + coll.getPath() + "'.", e );
        }
    }

    @Override
    public void processItem( WalkerContext context, StorageItem item )
    {
        if ( item instanceof StorageFileItem )
        {
            try
            {
                mdHelper.processFile( item.getPath() );
            }
            catch ( Exception e )
            {
                logger.warn( "Error occured while processing item '" + item.getPath() + "'.", e );
            }
        }
    }

    @Override
    public void onCollectionExit( WalkerContext context, StorageCollectionItem coll )
    {
        try
        {
            mdHelper.onDirExit( coll.getPath() );

            if ( coll.list().size() == 0 )
            {
                repository.deleteItem( false, new ResourceStoreRequest( coll ) );
            }
        }
        catch ( Exception e )
        {
            logger.warn( "Error occured while existing collection '" + coll.getPath() + "'.", e );
        }
    }
}
