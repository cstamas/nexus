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
package org.sonatype.nexus.security.filter;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.security.web.ShiroSecurityFilter;

/**
 * This filter simply behaves according Nexus configuration.
 * 
 * @author cstamas
 */
public class NexusJSecurityFilter
    extends ShiroSecurityFilter
{
    public static final String REQUEST_IS_AUTHZ_REJECTED = "request.is.authz.rejected";

    public NexusJSecurityFilter()
    {
        // not setting configClassName explicitly, so we can use either configRole or configClassName
    }

    @Override
    protected boolean shouldNotFilter( ServletRequest request )
        throws ServletException
    {
        return !( (NexusConfiguration) getAttribute( NexusConfiguration.class.getName() ) ).isSecurityEnabled();
    }

    protected Object getAttribute( String key )
    {
        return this.getFilterConfig().getServletContext().getAttribute( key );
    }
}
