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
package org.sonatype.nexus.integrationtests.nexus533;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceWeeklyResource;
import org.sonatype.nexus.tasks.descriptors.UpdateIndexTaskDescriptor;

import edu.emory.mathcs.backport.java.util.Arrays;

public class Nexus533TaskWeeklyIT
    extends AbstractNexusTasksIntegrationIT<ScheduledServiceWeeklyResource>
{

    private static ScheduledServiceWeeklyResource scheduledTask;

    @Override
    public ScheduledServiceWeeklyResource getTaskScheduled()
    {
        if ( scheduledTask == null )
        {
            scheduledTask = new ScheduledServiceWeeklyResource();
            scheduledTask.setEnabled( true );
            scheduledTask.setId( null );
            scheduledTask.setName( "taskWeekly" );
            scheduledTask.setSchedule( "weekly" );
            // A future date
            Date startDate = DateUtils.addDays( new Date(), 10 );
            startDate = DateUtils.round( startDate, Calendar.DAY_OF_MONTH );
            scheduledTask.setStartDate( String.valueOf( startDate.getTime() ) );
            scheduledTask.setRecurringTime( "03:30" );
            scheduledTask.setRecurringDay( Arrays.asList( new String[] { "monday", "wednesday", "friday" } ) );

            scheduledTask.setTypeId( UpdateIndexTaskDescriptor.ID );

            ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
            prop.setKey( "repositoryOrGroupId" );
            prop.setValue( "all_repo" );
            scheduledTask.addProperty( prop );
        }
        return scheduledTask;
    }

    @Override
    public void updateTask( ScheduledServiceWeeklyResource scheduledTask )
    {
        scheduledTask.setRecurringTime( "00:00" );
    }

}
