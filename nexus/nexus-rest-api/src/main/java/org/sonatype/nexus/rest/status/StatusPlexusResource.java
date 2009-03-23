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
package org.sonatype.nexus.rest.status;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.rest.authentication.AbstractUIPermissionCalculatingPlexusResource;
import org.sonatype.nexus.rest.model.NexusAuthenticationClientPermissions;
import org.sonatype.nexus.rest.model.StatusConfigurationValidationResponse;
import org.sonatype.nexus.rest.model.StatusResource;
import org.sonatype.nexus.rest.model.StatusResourceResponse;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.security.rest.model.AuthenticationClientPermissions;

@Component( role = ManagedPlexusResource.class, hint = "StatusPlexusResource" )
public class StatusPlexusResource
    extends AbstractUIPermissionCalculatingPlexusResource
    implements ManagedPlexusResource
{

    @Requirement
    private Nexus nexus;
    
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/status";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:status]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        SystemStatus status = this.nexus.getSystemStatus();

        StatusResource resource = new StatusResource();

        resource.setAppName( status.getAppName() );

        resource.setFormattedAppName( status.getFormattedAppName() );

        resource.setVersion( status.getVersion() );

        resource.setApiVersion( status.getApiVersion() );

        resource.setEditionLong( status.getEditionLong() );

        resource.setEditionShort( status.getEditionShort() );

        resource.setState( status.getState().toString() );

        resource.setOperationMode( status.getOperationMode().toString() );

        resource.setInitializedAt( status.getInitializedAt() );

        resource.setStartedAt( status.getStartedAt() );

        resource.setLastConfigChange( status.getLastConfigChange() );

        resource.setFirstStart( status.isFirstStart() );

        resource.setInstanceUpgraded( status.isInstanceUpgraded() );

        resource.setConfigurationUpgraded( status.isConfigurationUpgraded() );

        resource.setErrorCause( spit( status.getErrorCause() ) );

        if ( status.getConfigurationValidationResponse() != null )
        {
            resource.setConfigurationValidationResponse( new StatusConfigurationValidationResponse() );

            resource.getConfigurationValidationResponse().setValid(
                status.getConfigurationValidationResponse().isValid() );

            resource.getConfigurationValidationResponse().setModified(
                status.getConfigurationValidationResponse().isModified() );

            for ( ValidationMessage msg : status.getConfigurationValidationResponse().getValidationErrors() )
            {
                resource.getConfigurationValidationResponse().addValidationError( msg.toString() );
            }
            for ( ValidationMessage msg : status.getConfigurationValidationResponse().getValidationWarnings() )
            {
                resource.getConfigurationValidationResponse().addValidationWarning( msg.toString() );
            }
        }

        resource.setClientPermissions( getNexusClientPermissionsForCurrentUser( request ) );

        resource.setBaseUrl( getContextRoot( request ).toString() );

        StatusResourceResponse result = new StatusResourceResponse();

        result.setData( resource );

        return result;
    }
    
    // FIXME: DIRTY HACK, Modello is not all that model extending friendly 
    private NexusAuthenticationClientPermissions getNexusClientPermissionsForCurrentUser( Request request ) throws ResourceException
    {
        NexusAuthenticationClientPermissions nexusAuthClientPerms = new NexusAuthenticationClientPermissions();
        
        AuthenticationClientPermissions clientPerms = getClientPermissionsForCurrentUser( request );
        
        nexusAuthClientPerms.setLoggedIn( clientPerms.isLoggedIn() );
        nexusAuthClientPerms.setLoggedInUsername( clientPerms.getLoggedInUsername() );
        nexusAuthClientPerms.setLoggedInUserSource( clientPerms.getLoggedInUserSource() );
        nexusAuthClientPerms.setPermissions( clientPerms.getPermissions() );
        
        return nexusAuthClientPerms;
    }

    private String spit( Throwable t )
    {
        if ( t == null )
        {
            return null;
        }
        else
        {
            StringWriter sw = new StringWriter();

            t.printStackTrace( new PrintWriter( sw ) );

            return sw.toString();
        }

    }
}
