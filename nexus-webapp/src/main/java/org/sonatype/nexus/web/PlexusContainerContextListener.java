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
package org.sonatype.nexus.web;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.PropertyConfigurator;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;

/**
 * @author Juven Xu
 */
public class PlexusContainerContextListener
    implements ServletContextListener
{
    private static final String KEY_PLEXUS = "plexus";

    PlexusContainerConfigurationUtils plexusContainerConfigurationUtils = new PlexusContainerConfigurationUtils();

    PlexusContainerUtils plexusContainerUtils = new PlexusContainerUtils();

    public void contextInitialized( ServletContextEvent sce )
    {
        ServletContext context = sce.getServletContext();

        ContainerConfiguration plexusContainerConfiguration = plexusContainerConfigurationUtils
            .buildContainerConfiguration( context );

        NexusWorkDirUtils.setUpNexusWorkDir( plexusContainerConfiguration.getContext() );

        try
        {
            initizlizePlexusContainer( context, plexusContainerConfiguration );
        }
        catch ( PlexusContainerException e )
        {
            throw new IllegalStateException( "Could start plexus container", e );
        }
    }

    public void contextDestroyed( ServletContextEvent sce )
    {
        plexusContainerUtils.stopContainer();
    }

    private void initizlizePlexusContainer( ServletContext context, ContainerConfiguration configuration )
        throws PlexusContainerException
    {
        String path = "/WEB-INF/log4j.properties";

        try
        {
            // a simple logger only for plexus container, note that nexus will use another log4j.properties which will
            // supersede this one
            URL simpleLog4j = context.getResource( path );

            PropertyConfigurator.configure( simpleLog4j );
        }
        catch ( MalformedURLException e )
        {
            context.log( "Could not load simple log config from: " + path, e );
        }

        PlexusContainer plexusContainer = plexusContainerUtils.startContainer( configuration );

        context.setAttribute( KEY_PLEXUS, plexusContainer );
    }
}
