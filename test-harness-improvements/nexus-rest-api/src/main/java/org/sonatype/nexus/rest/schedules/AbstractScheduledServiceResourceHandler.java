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
package org.sonatype.nexus.rest.schedules;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.maven.tasks.SnapshotRemoverTask;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.ScheduledServiceAdvancedResource;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceDailyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceMonthlyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceOnceResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceWeeklyResource;
import org.sonatype.nexus.tasks.ClearCacheTask;
import org.sonatype.nexus.tasks.EvictUnusedProxiedItemsTask;
import org.sonatype.nexus.tasks.PublishIndexesTask;
import org.sonatype.nexus.tasks.PurgeTimeline;
import org.sonatype.nexus.tasks.RebuildAttributesTask;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;
import org.sonatype.scheduling.schedules.CronSchedule;
import org.sonatype.scheduling.schedules.DailySchedule;
import org.sonatype.scheduling.schedules.MonthlySchedule;
import org.sonatype.scheduling.schedules.OnceSchedule;
import org.sonatype.scheduling.schedules.Schedule;
import org.sonatype.scheduling.schedules.WeeklySchedule;

public class AbstractScheduledServiceResourceHandler
    extends AbstractNexusResourceHandler
{
    /** Schedule Type Off. */
    public static final String SCHEDULE_TYPE_MANUAL = "manual";

    /** Schedule Type Once. */
    public static final String SCHEDULE_TYPE_ONCE = "once";

    /** Schedule Type Daily. */
    public static final String SCHEDULE_TYPE_DAILY = "daily";

    /** Schedule Type Weekly. */
    public static final String SCHEDULE_TYPE_WEEKLY = "weekly";

    /** Schedule Type Monthly. */
    public static final String SCHEDULE_TYPE_MONTHLY = "monthly";

    /** Schedule Type Advanced. */
    public static final String SCHEDULE_TYPE_ADVANCED = "advanced";

    /**
     * Type property resource: string
     */
    public static final String PROPERTY_TYPE_STRING = "string";

    /**
     * Type property resource: number
     */
    public static final String PROPERTY_TYPE_NUMBER = "number";

    /**
     * Type property resource: number
     */
    public static final String PROPERTY_TYPE_BOOLEAN = "boolean";

    /**
     * Type property resource: date
     */
    public static final String PROPERTY_TYPE_DATE = "date";

    /**
     * Type property resource: repository
     */
    public static final String PROPERTY_TYPE_REPO = "repo";

    /**
     * Type property resource: repositoryGroup
     */
    public static final String PROPERTY_TYPE_REPO_GROUP = "group";

    /**
     * Type property resource: repo-or-group
     */
    public static final String PROPERTY_TYPE_REPO_OR_GROUP = "repo-or-group";

    public static final Integer DAY_OF_MONTH_LAST = new Integer( 999 );

    private DateFormat timeFormat = new SimpleDateFormat( "HH:mm" );

    protected Map<Class<?>, String> serviceNames = new HashMap<Class<?>, String>();
    {
        serviceNames.put( PublishIndexesTask.class, "Publish Indexes" );
        serviceNames.put( ReindexTask.class, "Reindex Repositories" );
        serviceNames.put( RebuildAttributesTask.class, "Rebuild Repository Attributes" );
        serviceNames.put( ClearCacheTask.class, "Clear Repository Caches" );
        serviceNames.put( SnapshotRemoverTask.class, "Remove Snapshots From Repository" );
        serviceNames.put( EvictUnusedProxiedItemsTask.class, "Evict Unused Proxied Items From Repository Caches" );
        serviceNames.put( PurgeTimeline.class, "Purge Nexus Timeline" );
    }

    /**
     * Standard constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public AbstractScheduledServiceResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    protected String getServiceTypeName( Class<?> serviceTypeId )
    {
        if ( serviceNames.containsKey( serviceTypeId ) )
        {
            return serviceNames.get( serviceTypeId );
        }
        else
        {
            return serviceTypeId.getName();
        }
    }

    protected String getScheduleShortName( Schedule schedule )
    {
        if ( schedule == null )
        {
            return SCHEDULE_TYPE_MANUAL;
        }
        else if ( OnceSchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            return SCHEDULE_TYPE_ONCE;
        }
        else if ( DailySchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            return SCHEDULE_TYPE_DAILY;
        }
        else if ( WeeklySchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            return SCHEDULE_TYPE_WEEKLY;
        }
        else if ( MonthlySchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            return SCHEDULE_TYPE_MONTHLY;
        }
        else if ( CronSchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            return SCHEDULE_TYPE_ADVANCED;
        }
        else
        {
            return schedule.getClass().getName();
        }
    }

    protected String formatDate( Date date )
    {
        return Long.toString( date.getTime() );
    }

    protected String formatTime( Date date )
    {
        return timeFormat.format( date );
    }

    protected List<ScheduledServicePropertyResource> formatServiceProperties( Map<String, String> map )
    {
        List<ScheduledServicePropertyResource> list = new ArrayList<ScheduledServicePropertyResource>();

        for ( String key : map.keySet() )
        {
            ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
            prop.setId( key );
            prop.setValue( map.get( key ) );
            list.add( prop );
        }

        return list;
    }

    protected List<String> formatRecurringDayOfWeek( Set<Integer> days )
    {
        List<String> list = new ArrayList<String>();

        for ( Integer day : days )
        {
            switch ( day.intValue() )
            {
                case 0:
                {
                    list.add( "sunday" );
                    break;
                }
                case 1:
                {
                    list.add( "monday" );
                    break;
                }
                case 2:
                {
                    list.add( "tuesday" );
                    break;
                }
                case 3:
                {
                    list.add( "wednesday" );
                    break;
                }
                case 4:
                {
                    list.add( "thursday" );
                    break;
                }
                case 5:
                {
                    list.add( "friday" );
                    break;
                }
                case 6:
                {
                    list.add( "saturday" );
                    break;
                }
            }
        }

        return list;
    }

    protected Set<Integer> formatRecurringDayOfWeek( List<String> days )
    {
        Set<Integer> set = new HashSet<Integer>();

        for ( String day : days )
        {
            if ( "sunday".equals( day ) )
            {
                set.add( new Integer( 0 ) );
            }
            else if ( "monday".equals( day ) )
            {
                set.add( new Integer( 1 ) );
            }
            else if ( "tuesday".equals( day ) )
            {
                set.add( new Integer( 2 ) );
            }
            else if ( "wednesday".equals( day ) )
            {
                set.add( new Integer( 3 ) );
            }
            else if ( "thursday".equals( day ) )
            {
                set.add( new Integer( 4 ) );
            }
            else if ( "friday".equals( day ) )
            {
                set.add( new Integer( 5 ) );
            }
            else if ( "saturday".equals( day ) )
            {
                set.add( new Integer( 6 ) );
            }
        }

        return set;
    }

    protected List<String> formatRecurringDayOfMonth( Set<Integer> days )
    {
        List<String> list = new ArrayList<String>();

        for ( Integer day : days )
        {
            if ( DAY_OF_MONTH_LAST.equals( day ) )
            {
                list.add( "last" );
            }
            else
            {
                list.add( String.valueOf( day ) );
            }
        }

        return list;
    }

    protected Set<Integer> formatRecurringDayOfMonth( List<String> days )
    {
        Set<Integer> set = new HashSet<Integer>();

        for ( String day : days )
        {
            if ( "last".equals( day ) )
            {
                set.add( DAY_OF_MONTH_LAST );
            }
            else
            {
                set.add( Integer.valueOf( day ) );
            }
        }

        return set;
    }

    protected Date parseDate( String date, String time )
    {
        Calendar cal = Calendar.getInstance();
        Calendar timeCalendar = Calendar.getInstance();

        try
        {
            timeCalendar.setTime( timeFormat.parse( time ) );

            cal.setTime( new Date( Long.parseLong( date ) ) );
            cal.add( Calendar.HOUR_OF_DAY, timeCalendar.get( Calendar.HOUR_OF_DAY ) );
            cal.add( Calendar.MINUTE, timeCalendar.get( Calendar.MINUTE ) );
        }
        catch ( ParseException e )
        {
            cal = null;
        }

        return cal == null ? null : cal.getTime();
    }

    public String getModelName( ScheduledServiceBaseResource model )
    {
        return model.getName();
    }

    public SchedulerTask<?> getModelNexusTask( ScheduledServiceBaseResource model )
    {
        String serviceType = model.getTypeId();

        SchedulerTask<?> task = getNexus().createTaskInstance( serviceType );

        for ( Iterator iter = model.getProperties().iterator(); iter.hasNext(); )
        {
            ScheduledServicePropertyResource prop = (ScheduledServicePropertyResource) iter.next();
            task.addParameter( prop.getId(), prop.getValue() );
        }

        return task;
    }

    public Schedule getModelSchedule( ScheduledServiceBaseResource model )
        throws ParseException
    {
        Schedule schedule = null;

        if ( ScheduledServiceAdvancedResource.class.isAssignableFrom( model.getClass() ) )
        {
            schedule = new CronSchedule( ( (ScheduledServiceAdvancedResource) model ).getCronCommand() );
        }
        else if ( ScheduledServiceMonthlyResource.class.isAssignableFrom( model.getClass() ) )
        {
            schedule = new MonthlySchedule(
                parseDate(
                    ( (ScheduledServiceMonthlyResource) model ).getStartDate(),
                    ( (ScheduledServiceMonthlyResource) model ).getRecurringTime() ),
                null,
                formatRecurringDayOfMonth( ( (ScheduledServiceMonthlyResource) model ).getRecurringDay() ) );
        }
        else if ( ScheduledServiceWeeklyResource.class.isAssignableFrom( model.getClass() ) )
        {
            schedule = new WeeklySchedule(
                parseDate(
                    ( (ScheduledServiceWeeklyResource) model ).getStartDate(),
                    ( (ScheduledServiceWeeklyResource) model ).getRecurringTime() ),
                null,
                formatRecurringDayOfWeek( ( (ScheduledServiceWeeklyResource) model ).getRecurringDay() ) );
        }
        else if ( ScheduledServiceDailyResource.class.isAssignableFrom( model.getClass() ) )
        {
            schedule = new DailySchedule( parseDate(
                ( (ScheduledServiceDailyResource) model ).getStartDate(),
                ( (ScheduledServiceDailyResource) model ).getRecurringTime() ), null );
        }
        else if ( ScheduledServiceOnceResource.class.isAssignableFrom( model.getClass() ) )
        {
            schedule = new OnceSchedule( parseDate(
                ( (ScheduledServiceOnceResource) model ).getStartDate(),
                ( (ScheduledServiceOnceResource) model ).getStartTime() ) );
        }

        return schedule;
    }
    
    public <T> ScheduledServiceBaseResource getServiceRestModel( ScheduledTask<T> task )
    {
        ScheduledServiceBaseResource resource = null;
        
        if ( task.getSchedule() == null )
        {
            resource = new ScheduledServiceBaseResource();
        }
        else if ( OnceSchedule.class.isAssignableFrom( task.getSchedule().getClass() ) )
        {
            resource = new ScheduledServiceOnceResource();

            OnceSchedule taskSchedule = (OnceSchedule) task.getSchedule();
            ScheduledServiceOnceResource res = (ScheduledServiceOnceResource) resource;

            res.setStartDate( formatDate( taskSchedule.getStartDate() ) );
            res.setStartTime( formatTime( taskSchedule.getStartDate() ) );
        }
        else if ( DailySchedule.class.isAssignableFrom( task.getSchedule().getClass() ) )
        {
            resource = new ScheduledServiceDailyResource();

            DailySchedule taskSchedule = (DailySchedule) task.getSchedule();
            ScheduledServiceDailyResource res = (ScheduledServiceDailyResource) resource;

            res.setStartDate( formatDate( taskSchedule.getStartDate() ) );
            res.setRecurringTime( formatTime( taskSchedule.getStartDate() ) );
        }
        else if ( WeeklySchedule.class.isAssignableFrom( task.getSchedule().getClass() ) )
        {
            resource = new ScheduledServiceWeeklyResource();

            WeeklySchedule taskSchedule = (WeeklySchedule) task.getSchedule();
            ScheduledServiceWeeklyResource res = (ScheduledServiceWeeklyResource) resource;

            res.setStartDate( formatDate( taskSchedule.getStartDate() ) );
            res.setRecurringTime( formatTime( taskSchedule.getStartDate() ) );
            res.setRecurringDay( formatRecurringDayOfWeek( taskSchedule.getDaysToRun() ) );
        }
        else if ( MonthlySchedule.class.isAssignableFrom( task.getSchedule().getClass() ) )
        {
            resource = new ScheduledServiceMonthlyResource();

            MonthlySchedule taskSchedule = (MonthlySchedule) task.getSchedule();
            ScheduledServiceMonthlyResource res = (ScheduledServiceMonthlyResource) resource;

            res.setStartDate( formatDate( taskSchedule.getStartDate() ) );
            res.setRecurringTime( formatTime( taskSchedule.getStartDate() ) );
            res.setRecurringDay( formatRecurringDayOfMonth( taskSchedule.getDaysToRun() ) );
        }
        else if ( CronSchedule.class.isAssignableFrom( task.getSchedule().getClass() ) )
        {
            resource = new ScheduledServiceAdvancedResource();

            CronSchedule taskSchedule = (CronSchedule) task.getSchedule();
            ScheduledServiceAdvancedResource res = (ScheduledServiceAdvancedResource) resource;

            res.setCronCommand( taskSchedule.getCronString() );
        }

        if ( resource != null )
        {
            resource.setId( task.getId() );
            resource.setEnabled( task.isEnabled() );
            resource.setName( task.getName() );
            resource.setSchedule( getScheduleShortName( task.getSchedule() ) );
            resource.setTypeId( task.getType().getName() );
            resource.setProperties( formatServiceProperties( task.getTaskParams() ) );
        }
        
        return resource;
    }
}
