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

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.model.ScheduledServiceAdvancedResource;
import org.sonatype.nexus.rest.model.ScheduledServiceDailyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceMonthlyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceOnceResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceWeeklyResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class ScheduledServiceResponseTest
    extends TestCase
{

    protected XStream xstream;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        // create and configure XStream for JSON
        xstream = XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void testNoScheduledService()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"id\":null,\"name\":\"test\",\"typeId\":\"Synchronize Repositories\",\"schedule\":\"none\"}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );

        ScheduledServiceResourceResponse response =
            (ScheduledServiceResourceResponse) representation.getPayload( new ScheduledServiceResourceResponse() );

        assert response.getData().getId() == null;
        assert response.getData().getName().equals( "test" );
        assert response.getData().getTypeId().equals( "Synchronize Repositories" );
        assert response.getData().getSchedule().equals( "none" );
    }

    public void testOnceScheduledService()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"id\":null,\"name\":\"test\",\"typeId\":\"Synchronize Repositories\",\"schedule\":\"once\",\"startDate\":\"1210651200000\",\"startTime\":\"12:30\"}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );

        ScheduledServiceResourceResponse response =
            (ScheduledServiceResourceResponse) representation.getPayload( new ScheduledServiceResourceResponse() );

        assert response.getData().getId() == null;
        assert response.getData().getName().equals( "test" );
        assert response.getData().getTypeId().equals( "Synchronize Repositories" );
        assert response.getData().getSchedule().equals( "once" );
        assert ( (ScheduledServiceOnceResource) response.getData() ).getStartDate().equals( "1210651200000" );
        assert ( (ScheduledServiceOnceResource) response.getData() ).getStartTime().equals( "12:30" );
    }

    public void testDailyScheduledService()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"id\":null,\"name\":\"test\",\"typeId\":\"Synchronize Repositories\",\"schedule\":\"daily\",\"startDate\":\"1210651200000\",\"recurringTime\":\"12:30\"}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );

        ScheduledServiceResourceResponse response =
            (ScheduledServiceResourceResponse) representation.getPayload( new ScheduledServiceResourceResponse() );

        assert response.getData().getId() == null;
        assert response.getData().getName().equals( "test" );
        assert response.getData().getTypeId().equals( "Synchronize Repositories" );
        assert response.getData().getSchedule().equals( "daily" );
        assert ( (ScheduledServiceDailyResource) response.getData() ).getStartDate().equals( "1210651200000" );
        assert ( (ScheduledServiceDailyResource) response.getData() ).getRecurringTime().equals( "12:30" );
    }

    public void testWeeklyScheduledService()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"id\":null,\"name\":\"test\",\"typeId\":\"Synchronize Repositories\",\"schedule\":\"weekly\",\"startDate\":\"1210651200000\",\"recurringTime\":\"12:30\",\"recurringDay\":[\"Monday\",\"Wednesday\"]}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );

        ScheduledServiceResourceResponse response =
            (ScheduledServiceResourceResponse) representation.getPayload( new ScheduledServiceResourceResponse() );

        assert response.getData().getId() == null;
        assert response.getData().getName().equals( "test" );
        assert response.getData().getTypeId().equals( "Synchronize Repositories" );
        assert response.getData().getSchedule().equals( "weekly" );
        assert ( (ScheduledServiceWeeklyResource) response.getData() ).getStartDate().equals( "1210651200000" );
        assert ( (ScheduledServiceWeeklyResource) response.getData() ).getRecurringTime().equals( "12:30" );
    }

    public void testMonthlyScheduledService()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"id\":null,\"name\":\"test\",\"typeId\":\"Synchronize Repositories\",\"schedule\":\"monthly\",\"startDate\":\"1210651200000\",\"recurringTime\":\"12:30\",\"recurringDay\":[\"1\",\"2\"]}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );

        ScheduledServiceResourceResponse response =
            (ScheduledServiceResourceResponse) representation.getPayload( new ScheduledServiceResourceResponse() );

        assert response.getData().getId() == null;
        assert response.getData().getName().equals( "test" );
        assert response.getData().getTypeId().equals( "Synchronize Repositories" );
        assert response.getData().getSchedule().equals( "monthly" );
        assert ( (ScheduledServiceMonthlyResource) response.getData() ).getStartDate().equals( "1210651200000" );
        assert ( (ScheduledServiceMonthlyResource) response.getData() ).getRecurringTime().equals( "12:30" );
    }

    public void testAdvancedScheduledService()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"id\":null,\"name\":\"test\",\"typeId\":\"Synchronize Repositories\",\"schedule\":\"advanced\",\"cronCommand\":\"somecroncommand\"}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );

        ScheduledServiceResourceResponse response =
            (ScheduledServiceResourceResponse) representation.getPayload( new ScheduledServiceResourceResponse() );

        assert response.getData().getId() == null;
        assert response.getData().getName().equals( "test" );
        assert response.getData().getTypeId().equals( "Synchronize Repositories" );
        assert response.getData().getSchedule().equals( "advanced" );
        assert ( (ScheduledServiceAdvancedResource) response.getData() ).getCronCommand().equals( "somecroncommand" );
    }
    
    public void testAdvancedScheduledServiceWithProperties()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"id\":null,\"name\":\"test\",\"typeId\":\"Synchronize Repositories\",\"schedule\":\"advanced\",\"cronCommand\":\"somecroncommand\",\"properties\":[{\"id\":\"1\",\"value\":\"true\",\"@class\":\"org.sonatype.nexus.rest.model.ScheduledServicePropertyResource\"}]}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );
    
        ScheduledServiceResourceResponse response =
            (ScheduledServiceResourceResponse) representation.getPayload( new ScheduledServiceResourceResponse() );
    
        assert response.getData().getId() == null;
        assert response.getData().getName().equals( "test" );
        assert response.getData().getTypeId().equals( "Synchronize Repositories" );
        assert response.getData().getSchedule().equals( "advanced" );
        assert response.getData().getProperties().size() == 1;
        assert ( (ScheduledServicePropertyResource) response.getData().getProperties().get( 0 ) ).getId().equals( "1" );
        assert ( (ScheduledServicePropertyResource) response.getData().getProperties().get( 0 ) ).getValue().equals( "true" );
        assert ( (ScheduledServiceAdvancedResource) response.getData() ).getCronCommand().equals( "somecroncommand" );
    }
}
