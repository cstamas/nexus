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
package org.sonatype.nexus.events;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryEventLocalStatusChanged;
import org.sonatype.nexus.proxy.repository.LocalStatus;

/**
 * @author Juven Xu
 */
@Component( role = EventInspector.class, hint = "RepositoryEventLocalStatusChanged" )
public class RepositoryEventLocalStatusChangedInspector
    extends AbstractFeedRecorderEventInspector
{

    public boolean accepts( AbstractEvent evt )
    {
        if ( evt instanceof RepositoryEventLocalStatusChanged )
        {
            return true;
        }
        return false;
    }

    public void inspect( AbstractEvent evt )
    {
        RepositoryEventLocalStatusChanged revt = (RepositoryEventLocalStatusChanged) evt;

        StringBuffer sb = new StringBuffer( "The repository '" );

        sb.append( revt.getRepository().getName() );

        sb.append( "' (ID='" ).append( revt.getRepository().getId() ).append( "') was put " );

        if ( LocalStatus.IN_SERVICE.equals( revt.getRepository().getLocalStatus() ) )
        {
            sb.append( "IN SERVICE." );
        }
        else if ( LocalStatus.OUT_OF_SERVICE.equals( revt.getRepository().getLocalStatus() ) )
        {
            sb.append( "OUT OF SERVICE." );
        }
        else
        {
            sb.append( revt.getRepository().getLocalStatus().toString() ).append( "." );
        }

        sb.append( " The previous state was " );

        if ( LocalStatus.IN_SERVICE.equals( revt.getOldLocalStatus() ) )
        {
            sb.append( "IN SERVICE." );
        }
        else if ( LocalStatus.OUT_OF_SERVICE.equals( revt.getOldLocalStatus() ) )
        {
            sb.append( "OUT OF SERVICE." );
        }
        else
        {
            sb.append( revt.getOldLocalStatus().toString() ).append( "." );
        }

        getFeedRecorder().addSystemEvent( FeedRecorder.SYSTEM_REPO_LSTATUS_CHANGES_ACTION, sb.toString() );
    }

}
