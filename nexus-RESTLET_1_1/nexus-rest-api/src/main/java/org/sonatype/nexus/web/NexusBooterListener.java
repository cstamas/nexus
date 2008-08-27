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
package org.sonatype.nexus.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.plexus.PlexusContainer;
import org.sonatype.nexus.Nexus;

public class NexusBooterListener
    implements ServletContextListener
{

    public void contextInitialized( ServletContextEvent sce )
    {
        try
        {
            PlexusContainer c = (PlexusContainer) sce.getServletContext().getAttribute( "plexus" );
            
            Nexus nexus = (Nexus) c.lookup( Nexus.ROLE );
            
            sce.getServletContext().setAttribute( Nexus.ROLE, nexus );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( "Could not initialize Nexus.", e );
        }
    }

    public void contextDestroyed( ServletContextEvent sce )
    {
    }

}
