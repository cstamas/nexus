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
package org.sonatype.nexus.events;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.plexus.appevents.Event;

/**
 * 
 * TODO: TONI - SHOULD LIVE IN ITS OWN PLUGIN.
 * 
 * Event inspector that maintains indexes.
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "IndexerManagerEventInspector" )
public class IndexerManagerEventInspector
    extends AbstractEventInspector
{

    public boolean accepts( Event<?> evt )
    {
        // listen for STORE, CACHE, DELETE only
        return ( RepositoryItemEventStore.class.isAssignableFrom( evt.getClass() )
            || RepositoryItemEventCache.class.isAssignableFrom( evt.getClass() ) || RepositoryItemEventDelete.class
                                                                                                                   .isAssignableFrom( evt.getClass() ) );
    }

    public void inspect( Event<?> evt )
    {
        inspectForIndexerManager( evt );
    }

    private void inspectForIndexerManager( Event<?> evt )
    {
        try
        {
            RepositoryItemEvent ievt = (RepositoryItemEvent) evt;

            // should we sync at all
            if ( ievt.getRepository().isIndexable() )
            {
                if ( ievt instanceof RepositoryItemEventCache || ievt instanceof RepositoryItemEventStore )
                {

                    // getIndexerManager().addItemToIndex( ievt.getRepository(), ievt.getItem() );
                }
                else if ( ievt instanceof RepositoryItemEventDelete )
                {
                    // getIndexerManager().removeItemFromIndex( ievt.getRepository(), ievt.getItem() );
                }
            }
        }
        catch ( Exception e ) // TODO be more specific
        {
            getLogger().error( "Could not maintain index!", e );
        }
    }

}
