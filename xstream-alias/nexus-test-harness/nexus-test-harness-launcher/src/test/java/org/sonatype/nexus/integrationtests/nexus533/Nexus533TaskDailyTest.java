package org.sonatype.nexus.integrationtests.nexus533;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.sonatype.nexus.rest.model.ScheduledServiceDailyResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;

public class Nexus533TaskDailyTest
    extends AbstractNexusTasksIntegrationTest<ScheduledServiceDailyResource>
{

    private static ScheduledServiceDailyResource scheduledTask;

    @Override
    public ScheduledServiceDailyResource getTaskScheduled()
    {
        if ( scheduledTask == null )
        {
            scheduledTask = new ScheduledServiceDailyResource();
            scheduledTask.setEnabled( true );
            scheduledTask.setId( null );
            scheduledTask.setName( "taskOnce" );
            // A future date
            Date startDate = DateUtils.addDays( new Date(), 10 );
            startDate = DateUtils.round( startDate, Calendar.DAY_OF_MONTH );
            scheduledTask.setStartDate( String.valueOf( startDate.getTime() ) );
            scheduledTask.setRecurringTime( "03:30" );

            scheduledTask.setTypeId( "org.sonatype.nexus.tasks.ReindexTask" );

            ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
            prop.setId( "repositoryOrGroupId" );
            prop.setValue( "all_repo" );
            scheduledTask.addProperty( prop );
        }
        return scheduledTask;
    }

    @Override
    public void updateTask( ScheduledServiceDailyResource scheduledTask )
    {
        scheduledTask.setRecurringTime( "00:00" );
    }

}
