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
package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.configuration.model.CRepository;

public enum ProxyMode
{
    ALLOW,

    BLOCKED_AUTO,

    BLOKED_MANUAL;

    public boolean shouldProxy()
    {
        return ALLOW.equals( this );
    }

    public boolean shouldCheckRemoteStatus()
    {
        return ALLOW.equals( this ) || BLOCKED_AUTO.equals( this );
    }

    public boolean shouldAutoUnblock()
    {
        return BLOCKED_AUTO.equals( this );
    }

    public static ProxyMode fromModel( String string )
    {
        if ( CRepository.PROXY_MODE_ALLOW.equals( string ) )
        {
            return ALLOW;
        }
        else if ( CRepository.PROXY_MODE_BLOCKED_AUTO.equals( string ) )
        {
            return BLOCKED_AUTO;
        }
        else if ( CRepository.PROXY_MODE_BLOCKED_MANUAL.equals( string ) )
        {
            return BLOKED_MANUAL;
        }
        else
        {
            return null;
        }
    }

    public static String toModel( ProxyMode proxyMode )
    {
        return proxyMode.toString();
    }

    public String toString()
    {
        if ( ALLOW.equals( this ) )
        {
            return CRepository.PROXY_MODE_ALLOW;
        }
        else if ( BLOCKED_AUTO.equals( this ) )
        {
            return CRepository.PROXY_MODE_BLOCKED_AUTO;
        }
        else if ( BLOKED_MANUAL.equals( this ) )
        {
            return CRepository.PROXY_MODE_BLOCKED_MANUAL;
        }
        else
        {
            return null;
        }
    }

}
