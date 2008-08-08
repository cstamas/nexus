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
package org.sonatype.nexus.rest.roles;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.model.RoleResourceRequest;
import org.sonatype.nexus.rest.model.RoleResourceResponse;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class RoleTest
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

    public void testRequest()
        throws Exception
    {
        String jsonString =
            "{\"data\":{\"id\":null,\"name\":\"Test Role\",\"description\":\"This is a test role\",\"sessionTimeout\":50," +
            "\"roles\":[\"roleid\"],\"privileges\":[\"privid\"]}}}";
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, jsonString, MediaType.APPLICATION_JSON );
        
        RoleResourceRequest request = ( RoleResourceRequest ) representation.getPayload( new RoleResourceRequest() );

        assert request.getData().getId() == null;
        assert request.getData().getName().equals( "Test Role" );
        assert request.getData().getDescription().equals( "This is a test role" );
        assert request.getData().getSessionTimeout() == 50;
        assert request.getData().getRoles().size() == 1;
        assert ( ( String ) request.getData().getRoles().get( 0 ) ).equals( "roleid" );        
        assert request.getData().getPrivileges().size() == 1;
        assert ( ( String ) request.getData().getPrivileges().get( 0 ) ).equals( "privid" );
    }
}
