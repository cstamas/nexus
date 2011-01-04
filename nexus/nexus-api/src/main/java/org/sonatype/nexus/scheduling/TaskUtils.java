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
package org.sonatype.nexus.scheduling;

import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Utilities related to tasks.
 *
 * @author Alin Dreghiciu
 */
public class TaskUtils
{

    /**
     * Private constructor. Utility class are not ment to be instantiated.
     */
    private TaskUtils()
    {
        // utility class
    }

    /**
     * Checks if a property is a private property. Private properties are those properties that start with
     * {@link NexusTask#PRIVATE_PROP_PREFIX}.
     *
     * @param key property key
     *
     * @return true if the key defines a private property
     */
    public static boolean isPrivateProperty( final String key )
    {
        return key != null && key.startsWith( NexusTask.PRIVATE_PROP_PREFIX );
    }

    /**
     * Returns the alert email for a task. The alert email is stored as a private task patamater.
     *
     * @param task a task
     *
     * @return alert email if present, null otherwise
     */
    public static String getAlertEmail( final ScheduledTask<?> task )
    {
        if( task != null && task.getTaskParams() != null )
        {
            return task.getTaskParams().get( NexusTask.ALERT_EMAIL_KEY );
        }
        return null;
    }

    /**
     * Sets the alert email for a scheduled task. The alert email is stored as a private task parameter.
     *
     * @param task       a scheduled task
     * @param alertEmail alert email
     */
    public static void setAlertEmail( final ScheduledTask<?> task,
                                      final String alertEmail )
    {
        if( task != null && task.getTaskParams() != null )
        {
            if( alertEmail != null )
            {
                task.getTaskParams().put( NexusTask.ALERT_EMAIL_KEY, alertEmail );
            }
            else
            {
                task.getTaskParams().remove( NexusTask.ALERT_EMAIL_KEY );
            }
        }
    }

    /**
     * Sets the email address to which an email should be sent in case of task failure.
     * If the alert email is not set (null or empty) no email should be sent.<br/>
     * The alert email is stored as a private task parameter.
     *
     * @param task  a nexus task
     * @param email alert email address
     */
    public static void setAlertEmail( final SchedulerTask<?> task,
                                      final String email )
    {
        if( email == null || email.trim().length() == 0 )
        {
            task.getParameters().remove( NexusTask.ALERT_EMAIL_KEY );
        }
        else
        {
            task.addParameter( NexusTask.ALERT_EMAIL_KEY, email );
        }
    }

    /**
     * Sets the task id as a private task parameter.
     *
     * @param task a scheduled task
     * @param id   task id
     */
    public static void setId( final ScheduledTask<?> task,
                              final String id )
    {
        if( id == null || id.trim().length() == 0 )
        {
            task.getTaskParams().remove( NexusTask.ID_KEY );
        }
        else
        {
            task.getTaskParams().put( NexusTask.ID_KEY, id );
        }
    }

    /**
     * Sets the task id as a private task parameter.
     *
     * @param task a nexus task
     * @param id   task id
     */
    public static void setId( final SchedulerTask<?> task,
                              final String id )
    {
        if( id == null || id.trim().length() == 0 )
        {
            task.getParameters().remove( NexusTask.ID_KEY );
        }
        else
        {
            task.addParameter( NexusTask.ID_KEY, id );
        }
    }

    /**
     * Sets the task name as a private task parameter.
     *
     * @param task a scheduled task
     * @param name task name
     */
    public static void setName( final ScheduledTask<?> task,
                                final String name )
    {
        if( name == null || name.trim().length() == 0 )
        {
            task.getTaskParams().remove( NexusTask.NAME_KEY );
        }
        else
        {
            task.getTaskParams().put( NexusTask.NAME_KEY, name );
        }
    }

    /**
     * Sets the task name as a private task parameter.
     *
     * @param task a nexus task
     * @param name task name
     */
    public static void setName( final SchedulerTask<?> task,
                                final String name )
    {
        if( name == null || name.trim().length() == 0 )
        {
            task.getParameters().remove( NexusTask.NAME_KEY );
        }
        else
        {
            task.addParameter( NexusTask.NAME_KEY, name );
        }
    }

}
