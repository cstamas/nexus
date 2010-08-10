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
package org.sonatype.nexus.security.filter.authz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.rest.RemoteIPFinder;
import org.sonatype.nexus.security.filter.NexusJSecurityFilter;
import org.sonatype.nexus.web.NexusBooterListener;

/**
 * A filter that maps the action from the HTTP Verb.
 * 
 * @author cstamas
 */
public class HttpVerbMappingAuthorizationFilter
    extends PermissionsAuthorizationFilter
{

    private final Log logger = LogFactory.getLog( this.getClass() );

    private AuthcAuthzEvent currentAuthzEvt;

    private Map<String, String> mapping = new HashMap<String, String>();
    {
        mapping.put( "options", "read" ); // HTTP/1.1 "standard" method
        mapping.put( "head", "read" ); // HTTP/1.1 "standard" method
        mapping.put( "get", "read" ); // HTTP/1.1 "standard" method
        mapping.put( "put", "update" ); // HTTP/1.1 "standard" method
        mapping.put( "post", "create" ); // HTTP/1.1 "standard" method
        mapping.put( "delete", "delete" ); // HTTP/1.1 "standard" method (to be explicit, mapping is actuall identity)
        mapping.put( "mkcol", "create" ); // DAV method, to support Maven's DAV Wagon
    }

    protected Log getLogger()
    {
        return logger;
    }

    protected Nexus getNexus( ServletRequest request )
    {
        Nexus nexus = (Nexus) request.getAttribute( Nexus.class.getName() );

        if ( nexus == null )
        {
            nexus = NexusBooterListener.getNexus();
        }

        return nexus;
    }

    protected PlexusContainer getPlexusContainer()
    {
        return (PlexusContainer) getAttribute( PlexusConstants.PLEXUS_KEY );
    }

    protected NexusConfiguration getNexusConfiguration()
    {
        return (NexusConfiguration) getAttribute( NexusConfiguration.class.getName() );
    }

    protected Action getActionFromHttpVerb( String method )
    {
        if ( null == method )
        {
            return null;
        }

        method = method.toLowerCase();

        if ( mapping.containsKey( method ) )
        {
            method = mapping.get( method );
        }

        try
        {
            return Action.valueOf( method );
        }
        catch ( IllegalArgumentException e )
        {
            getLogger().info( "Unknown HTTP verb " + method + "! Only HTTP/1.1 standard verbs are supported!" );

            return null;
        }
    }

    protected Action getActionFromHttpVerb( ServletRequest request )
    {
        String action = ( (HttpServletRequest) request ).getMethod();

        return getActionFromHttpVerb( action );
    }

    protected String[] mapPerms( String[] perms, Action action )
    {
        if ( perms != null && perms.length > 0 && action != null )
        {
            String[] mappedPerms = new String[perms.length];

            for ( int i = 0; i < perms.length; i++ )
            {
                mappedPerms[i] = perms[i] + ":" + action;
            }

            if ( getLogger().isDebugEnabled() )
            {
                StringBuffer sb = new StringBuffer();

                for ( int i = 0; i < mappedPerms.length; i++ )
                {
                    sb.append( mappedPerms[i] );

                    sb.append( ", " );
                }

                getLogger().debug(
                    "MAPPED '" + action + "' action to permission: " + sb.toString().substring( 0, sb.length() - 2 ) );
            }

            return mappedPerms;
        }
        else
        {
            return perms;
        }
    }

    @Override
    public boolean isAccessAllowed( ServletRequest request, ServletResponse response, Object mappedValue )
        throws IOException
    {
        String[] perms = (String[]) mappedValue;

        Action action = getActionFromHttpVerb( request );

        if ( getSubject( request, response ) != null && action != null
            && super.isAccessAllowed( request, response, mapPerms( perms, action ) ) )
        {
            // I wanted to record all successful authz events here, but found that, if we do record here, there would be
            // too many feed entries, that's a pollution

            return true;
        }

        return false;
    }

    @Override
    protected boolean onAccessDenied( ServletRequest request, ServletResponse response )
        throws IOException
    {
        recordAuthzFailureEvent( request, response );

        request.setAttribute( NexusJSecurityFilter.REQUEST_IS_AUTHZ_REJECTED, Boolean.TRUE );

        return false;
    }

    private void recordAuthzFailureEvent( ServletRequest request, ServletResponse response )
    {
        Subject subject = getSubject( request, response );

        if ( getNexusConfiguration().getAnonymousUsername().equals( subject.getPrincipal() ) )
        {
            return;
        }

        Action action = getActionFromHttpVerb( request );

        String method = ( (HttpServletRequest) request ).getMethod();

        String msg =
            "Unable to authorize user [" + subject.getPrincipal() + "] for " + String.valueOf( action )
                + "(HTTP method \"" + method + "\") to " + ( (HttpServletRequest) request ).getRequestURI()
                + " from IP Address " + RemoteIPFinder.findIP( (HttpServletRequest) request );

        if ( isSimilarEvent( msg ) )
        {
            return;
        }

        getLogger().info( msg );

        AuthcAuthzEvent authzEvt = new AuthcAuthzEvent( FeedRecorder.SYSTEM_AUTHZ, msg );

        if ( HttpServletRequest.class.isAssignableFrom( request.getClass() ) )
        {
            String ip = RemoteIPFinder.findIP( (HttpServletRequest) request );

            if ( ip != null )
            {
                authzEvt.getEventContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, ip );
            }
        }

        Nexus nexus = getNexus( request );
        if ( nexus != null )
        {
            nexus.addAuthcAuthzEvent( authzEvt );
        }

        currentAuthzEvt = authzEvt;
    }

    private boolean isSimilarEvent( String msg )
    {
        if ( currentAuthzEvt == null )
        {
            return false;
        }

        if ( currentAuthzEvt.getMessage().equals( msg )
            && ( System.currentTimeMillis() - currentAuthzEvt.getEventDate().getTime() < 2000L ) )
        {
            return true;
        }

        return false;
    }

    protected Object getAttribute( String key )
    {
        return this.getFilterConfig().getServletContext().getAttribute( key );
    }

}
