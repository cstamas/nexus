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
package org.sonatype.nexus.feeds;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.artifact.NexusItemInfo;
import org.sonatype.nexus.timeline.Timeline;
import org.sonatype.nexus.timeline.TimelineFilter;

/**
 * A feed recorder that uses DefaultNexus to record feeds.
 * 
 * @author cstamas
 */
@Component( role = FeedRecorder.class )
public class DefaultFeedRecorder
    extends AbstractLogEnabled
    implements FeedRecorder
{
    public static final int DEFAULT_PAGE_SIZE = 40;

    public static final String REPOSITORY = "r";

    public static final String REPOSITORY_PATH = "path";

    public static final String REMOTE_URL = "rurl";

    public static final String CTX_PREFIX = "ctx.";

    public static final String ACTION = "action";

    public static final String MESSAGE = "message";

    public static final String DATE = "date";

    /**
     * Event type: repository
     */
    private static final String REPO_EVENT_TYPE = "REPO_EVENTS";

    private static final Set<String> REPO_EVENT_TYPE_SET = new HashSet<String>( 1 );
    {
        REPO_EVENT_TYPE_SET.add( REPO_EVENT_TYPE );
    }

    /**
     * Event type: system
     */
    private static final String SYSTEM_EVENT_TYPE = "SYSTEM";

    private static final Set<String> SYSTEM_EVENT_TYPE_SET = new HashSet<String>( 1 );
    {
        SYSTEM_EVENT_TYPE_SET.add( SYSTEM_EVENT_TYPE );
    }

    /**
     * The timeline for persistent events and feeds.
     */
    @Requirement
    private Timeline timeline;

    /**
     * DateFormat used to format dates in events.
     */
    private DateFormat eventDateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSSZ" );

    public void startService()
        throws Exception
    {
        timeline.startService();
    }

    public void stopService()
        throws Exception
    {
        timeline.stopService();
    }

    protected List<NexusArtifactEvent> getAisFromMaps( List<Map<String, String>> data )
    {
        List<NexusArtifactEvent> result = new ArrayList<NexusArtifactEvent>( data.size() );

        for ( Map<String, String> map : data )
        {
            NexusArtifactEvent nae = new NexusArtifactEvent();

            NexusItemInfo ai = new NexusItemInfo();

            ai.setRepositoryId( map.get( REPOSITORY ) );

            ai.setPath( map.get( REPOSITORY_PATH ) );

            ai.setRemoteUrl( map.get( REMOTE_URL ) );

            nae.setNexusItemInfo( ai );

            try
            {
                nae.setEventDate( eventDateFormat.parse( map.get( DATE ) ) );
            }
            catch ( ParseException e )
            {
                getLogger().warn( "Could not format event date!", e );

                nae.setEventDate( new Date() );
            }

            HashMap<String, Object> ctx = new HashMap<String, Object>();

            for ( String key : map.keySet() )
            {
                if ( key.startsWith( CTX_PREFIX ) )
                {
                    ctx.put( key.substring( 4 ), map.get( key ) );
                }
            }

            nae.setMessage( map.get( MESSAGE ) );

            nae.setEventContext( ctx );

            nae.setAction( map.get( ACTION ) );

            result.add( nae );
        }

        return result;
    }

    protected List<SystemEvent> getSesFromMaps( List<Map<String, String>> data )
    {
        List<SystemEvent> result = new ArrayList<SystemEvent>( data.size() );

        for ( Map<String, String> map : data )
        {
            SystemEvent se = new SystemEvent( map.get( ACTION ), map.get( MESSAGE ) );

            try
            {
                se.setEventDate( eventDateFormat.parse( map.get( DATE ) ) );
            }
            catch ( ParseException e )
            {
                getLogger().warn( "Could not format event date!", e );
            }

            HashMap<String, Object> ctx = new HashMap<String, Object>();

            for ( String key : map.keySet() )
            {
                if ( key.startsWith( CTX_PREFIX ) )
                {
                    ctx.put( key.substring( 4 ), map.get( key ) );
                }
            }

            se.getEventContext().putAll( ctx );

            result.add( se );
        }

        return result;
    }

    public List<Map<String, String>> getEvents( Set<String> types, Set<String> subtypes, Integer from, Integer count,
        TimelineFilter filter )
    {
        int cnt = count != null ? count : DEFAULT_PAGE_SIZE;

        if ( from != null )
        {
            return timeline.retrieve( from, cnt, types, subtypes, filter );
        }
        else
        {
            return timeline.retrieveNewest( cnt, types, subtypes, filter );
        }
    }

    public List<NexusArtifactEvent> getNexusArtifectEvents( Set<String> subtypes, Integer from, Integer count,
        TimelineFilter filter )
    {
        return getAisFromMaps( getEvents( REPO_EVENT_TYPE_SET, subtypes, from, count, filter ) );
    }

    public List<SystemEvent> getSystemEvents( Set<String> subtypes, Integer from, Integer count, TimelineFilter filter )
    {
        return getSesFromMaps( getEvents( SYSTEM_EVENT_TYPE_SET, subtypes, from, count, filter ) );
    }
    
    private void putContext( Map<String, String> map, Map<String, Object> context )
    {
        for ( String key : context.keySet() )
        {
            Object value = context.get( key );

            if ( value == null )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "The attribute with key '" + key + "' in event context is NULL!" );
                }

                value = "";
            }

            map.put( CTX_PREFIX + key, value.toString() );
        }
    }

    public void addSystemEvent( String action, String message )
    {
        SystemEvent event = new SystemEvent( action, message );

        addToTimeline( event );
    }

    public void addNexusArtifactEvent( NexusArtifactEvent nae )
    {
        Map<String, String> map = new HashMap<String, String>();

        map.put( REPOSITORY, nae.getNexusItemInfo().getRepositoryId() );

        map.put( REPOSITORY_PATH, nae.getNexusItemInfo().getPath() );

        if ( nae.getNexusItemInfo().getRemoteUrl() != null )
        {
            map.put( REMOTE_URL, nae.getNexusItemInfo().getRemoteUrl() );
        }

        putContext( map, nae.getEventContext() );

        if ( nae.getMessage() != null )
        {
            map.put( MESSAGE, nae.getMessage() );
        }

        map.put( DATE, eventDateFormat.format( nae.getEventDate() ) );

        map.put( ACTION, nae.getAction() );

        addToTimeline( map, REPO_EVENT_TYPE, nae.getAction() );
    }

    public SystemProcess systemProcessStarted( String action, String message )
    {
        SystemProcess event = new SystemProcess( action, message, new Date() );

        addToTimeline( event );

        getLogger().info( event.getMessage() );

        return event;
    }

    public void systemProcessFinished( SystemProcess prc )
    {
        prc.finished();

        addToTimeline( prc );

        getLogger().info( prc.getMessage() );
    }

    public void systemProcessBroken( SystemProcess prc, Throwable e )
    {
        prc.broken( e );

        addToTimeline( prc );

        getLogger().info( prc.getMessage(), e );
    }

    protected void addToTimeline( SystemEvent se )
    {
        Map<String, String> map = new HashMap<String, String>();

        putContext( map, se.getEventContext() );

        map.put( DATE, eventDateFormat.format( se.getEventDate() ) );

        map.put( ACTION, se.getAction() );

        map.put( MESSAGE, se.getMessage() );

        addToTimeline( map, SYSTEM_EVENT_TYPE, se.getAction() );
    }

    protected void addToTimeline( Map<String, String> map, String t1, String t2 )
    {
        timeline.add( System.currentTimeMillis(), t1, t2, map );
    }

}
