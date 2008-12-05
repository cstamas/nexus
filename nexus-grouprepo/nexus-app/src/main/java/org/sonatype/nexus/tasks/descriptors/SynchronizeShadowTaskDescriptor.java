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
package org.sonatype.nexus.tasks.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

@Component( role = ScheduledTaskDescriptor.class, hint = "SynchronizeShadow", description = "Synchronize Shadow Repository" )
public class SynchronizeShadowTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{
    public static final String ID = "SynchronizeShadowsTask";

    @Requirement( role = ScheduledTaskPropertyDescriptor.class, hint = "Shadow" )
    private ScheduledTaskPropertyDescriptor shadowId;

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Synchronize Shadow Repository";
    }

    public List<ScheduledTaskPropertyDescriptor> getPropertyDescriptors()
    {
        List<ScheduledTaskPropertyDescriptor> properties = new ArrayList<ScheduledTaskPropertyDescriptor>();

        properties.add( shadowId );

        return properties;
    }
}
