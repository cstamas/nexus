package org.sonatype.nexus.integrationtests.nexus533;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ReindexTaskDescriptor;
import org.testng.annotations.Test;

public class Nexus533TaskManualTest
    extends AbstractNexusTasksIntegrationTest<ScheduledServiceBaseResource>
{

    private static ScheduledServiceBaseResource scheduledTask;

    @Test
    public void foo()
    {
        System.out.println( "foo" );
    }
    
    @Override
    public ScheduledServiceBaseResource getTaskScheduled()
    {
        if ( scheduledTask == null )
        {
            scheduledTask = new ScheduledServiceBaseResource();
            scheduledTask.setEnabled( true );
            scheduledTask.setId( null );
            scheduledTask.setName( "taskManual" );
            scheduledTask.setSchedule( "manual" );
            // A future date
            Date startDate = DateUtils.addDays( new Date(), 10 );
            startDate = DateUtils.round( startDate, Calendar.DAY_OF_MONTH );

            scheduledTask.setTypeId( ReindexTaskDescriptor.ID );

            ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
            prop.setId( "repositoryOrGroupId" );
            prop.setValue( "all_repo" );
            scheduledTask.addProperty( prop );
        }
        return scheduledTask;
    }

    @Override
    public void updateTask( ScheduledServiceBaseResource scheduledTask )
    {
        scheduledTask.getProperties().clear();

        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setId( "repositoryOrGroupId" );
        prop.setValue( "repo_nexus-test-harness-repo" );
        scheduledTask.addProperty( prop );
    }
  
}
