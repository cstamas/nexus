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
package org.sonatype.nexus.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.Form;
import org.restlet.data.Request;

public class RemoteIPFinder
{
    protected static final String FORWARD_HEADER = "X-Forwarded-For";
    
    public static String findIP( HttpServletRequest request )
    {
        String forwardedIP = getFirstForwardedIp( request.getHeader( FORWARD_HEADER ) );
        
        if ( forwardedIP != null )
        {
            return forwardedIP;
        }
        
        String remoteIP = request.getRemoteAddr();
        
        if ( remoteIP != null )
        {
            return remoteIP;
        }
        
        return null;
    }
    public static String findIP( Request request )
    {
        Form form = (Form) request.getAttributes().get("org.restlet.http.headers");
        
        String forwardedIP = getFirstForwardedIp( form.getFirstValue( FORWARD_HEADER ) );
        
        if ( forwardedIP != null )
        {
            return forwardedIP;
        }
        
        List<String> ipAddresses = request.getClientInfo().getAddresses();
        
        if ( ipAddresses.size() > 0 )
        {
            return ipAddresses.get( 0 );
        }
        
        return null;
    }
    
    protected static String getFirstForwardedIp( String forwardedFor )
    {
        if ( !StringUtils.isEmpty( forwardedFor ) )
        {
            String [] forwardedIps = forwardedFor.split( "," );
            
            return forwardedIps[0].trim();
        }
        
        return null;
    }
}
