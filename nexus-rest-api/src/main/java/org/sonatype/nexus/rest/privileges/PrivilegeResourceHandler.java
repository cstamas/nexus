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
package org.sonatype.nexus.rest.privileges;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.nexus.jsecurity.realms.NexusMethodRealm;
import org.sonatype.nexus.rest.model.PrivilegeStatusResourceResponse;

public class PrivilegeResourceHandler
    extends AbstractPrivilegeResourceHandler
{
    private String privilegeId;

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public PrivilegeResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        this.privilegeId = getRequest().getAttributes().get( PRIVILEGE_ID_KEY ).toString();
    }

    protected String getPrivilegeId()
    {
        return this.privilegeId;
    }

    /**
     * We are handling HTTP GET's
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * Method constructing and returning the privilege resource representation.
     */
    public Representation getRepresentationHandler( Variant variant )
    {
        PrivilegeStatusResourceResponse response = new PrivilegeStatusResourceResponse();
        
        CPrivilege priv = getNexusSecurity().readPrivilege( getPrivilegeId() );
        
        response.setData( nexusToRestModel( priv ) );
        
        return serialize( variant, response );
    }

    /**
     * This resource allows DELETE.
     */
    public boolean allowDelete()
    {
        return true;
    }

    /**
     * Delete a privilege.
     */
    public void delete()
    {
        CPrivilege privilege = getNexusSecurity().readPrivilege( getPrivilegeId() );
        
        if ( !privilege.getType().equals( NexusMethodRealm.PRIVILEGE_TYPE_METHOD ) )
        {
            getNexusSecurity().deletePrivilege( getPrivilegeId() );
        }
    }
}
