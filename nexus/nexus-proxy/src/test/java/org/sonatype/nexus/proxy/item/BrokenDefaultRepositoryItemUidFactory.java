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
package org.sonatype.nexus.proxy.item;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A default factory for UIDs. Is broken, as reported in NEXUS-3049.
 * 
 * @author cstamas
 */
@Component( role = RepositoryItemUidFactory.class, hint = "broken" )
public class BrokenDefaultRepositoryItemUidFactory
    implements RepositoryItemUidFactory
{
    /**
     * The registry.
     */
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    private final ConcurrentHashMap<String, WeakReference<RepositoryItemUid>> itemUidMap =
        new ConcurrentHashMap<String, WeakReference<RepositoryItemUid>>();

    public RepositoryItemUid createUid( Repository repository, String path )
    {
        // path corrections
        if ( !StringUtils.isEmpty( path ) )
        {
            if ( !path.startsWith( RepositoryItemUid.PATH_ROOT ) )
            {
                path = RepositoryItemUid.PATH_ROOT + path;
            }
        }
        else
        {
            path = RepositoryItemUid.PATH_ROOT;
        }

        String key = repository.getId() + ":" + path;

        RepositoryItemUid newGuy = new DefaultRepositoryItemUid( this, repository, path );

        itemUidMap.putIfAbsent( key, new WeakReference<RepositoryItemUid>( newGuy ) );

        RepositoryItemUid toBeReturned = itemUidMap.get( key ).get();

        if ( toBeReturned == null )
        {
            itemUidMap.put( key, new WeakReference<RepositoryItemUid>( newGuy ) );

            toBeReturned = newGuy;
        }

        // do cleansing of the map if needed, this call might do nothing or clean up the itemUidMap for gc'ed UIDs
        cleanUpItemUidMap( false );

        return toBeReturned;
    }

    public RepositoryItemUid createUid( String uidStr )
        throws IllegalArgumentException, NoSuchRepositoryException
    {
        if ( uidStr.indexOf( ":" ) > -1 )
        {
            String[] parts = uidStr.split( ":" );

            if ( parts.length == 2 )
            {
                Repository repository = repositoryRegistry.getRepository( parts[0] );

                return createUid( repository, parts[1] );
            }
            else
            {
                throw new IllegalArgumentException( uidStr
                    + " is malformed RepositoryItemUid! The proper format is '<repoId>:/path/to/something'." );
            }
        }
        else
        {
            throw new IllegalArgumentException( uidStr
                + " is malformed RepositoryItemUid! The proper format is '<repoId>:/path/to/something'." );
        }
    }

    public Map<String, RepositoryItemUid> getActiveUidMapSnapshot()
    {
        return Collections.emptyMap();
    }

    /**
     * Used in UTs only, NOT public method!
     * 
     * @return
     */
    public int getUidCount()
    {
        cleanUpItemUidMap( true );

        return itemUidMap.size();
    }

    // ==

    // =v=v=v=v=v=v= This part here probably needs polishing: the retention should depend on load too =v=v=v=v=v=v=

    private static final long ITEM_UID_MAP_RETENTION_TIME = 5000;

    private volatile long lastClearedItemUidMap;

    private synchronized void cleanUpItemUidMap( boolean force )
    {
        long now = System.currentTimeMillis();

        if ( force || ( now - lastClearedItemUidMap > ITEM_UID_MAP_RETENTION_TIME ) )
        {
            lastClearedItemUidMap = now;

            for ( Iterator<ConcurrentMap.Entry<String, WeakReference<RepositoryItemUid>>> i =
                itemUidMap.entrySet().iterator(); i.hasNext(); )
            {
                ConcurrentMap.Entry<String, WeakReference<RepositoryItemUid>> entry = i.next();

                if ( entry.getValue().get() == null )
                {
                    i.remove();
                }
            }
        }
    }

    // =^=^=^=^=^=^= This part here probably needs polishing: the retention should depend on load too =^=^=^=^=^=^=

}
