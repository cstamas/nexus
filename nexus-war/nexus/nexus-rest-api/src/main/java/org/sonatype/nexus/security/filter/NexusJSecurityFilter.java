package org.sonatype.nexus.security.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.jsecurity.web.PlexusJSecurityFilter;
import org.sonatype.nexus.Nexus;

/**
 * This filter simply behaves according Nexus configuration.
 * 
 * @author cstamas
 */
public class NexusJSecurityFilter
    extends PlexusJSecurityFilter
{
    public static final String REQUEST_IS_AUTHZ_REJECTED = "request.is.authz.rejected";

    private Nexus nexus;
    
    public NexusJSecurityFilter()
    {
        // not setting configClassName explicitly, so we can use either configRole or configClassName
    }

    protected final Nexus getNexus()
    {
        if ( nexus == null )
        {
            try
            {
                nexus = (Nexus) getPlexusContainer().lookup( Nexus.class );
            }
            catch ( ComponentLookupException e )
            {
                throw new IllegalStateException( "Cannot lookup Nexus!", e );
            }
        }

        return nexus;
    }

    @Override
    protected boolean shouldNotFilter( ServletRequest request )
        throws ServletException
    {
        return !getNexus().isSecurityEnabled();
    }

    @Override
    protected void doFilterInternal( ServletRequest servletRequest, ServletResponse servletResponse,
        FilterChain origChain )
        throws ServletException,
            IOException
    {
        servletRequest.setAttribute( Nexus.class.getName(), getNexus() );

        super.doFilterInternal( servletRequest, servletResponse, origChain );
    }
}
