package org.sonatype.nexus.rest.roles;

import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusRoleManager;
import org.sonatype.nexus.rest.model.PlexusRoleListResourceResponse;
import org.sonatype.nexus.rest.users.AbstractPlexusUserPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component(role=PlexusResource.class, hint="PlexusRoleListPlexusResource" )
public class PlexusRoleListPlexusResource
    extends AbstractPlexusUserPlexusResource
{
    @Requirement
    private PlexusRoleManager roleManager;

    public static final String SOURCE_ID_KEY = "sourceId";

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/plexus_roles/*", "authcBasic,perms[nexus:roles]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/plexus_roles/{" + SOURCE_ID_KEY + "}";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String source = this.getSourceId( request );

        // get roles for the source
        Set<PlexusRole> roles = this.roleManager.listRoles( source );

        if ( roles == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Role Source '" + source
                + "' could not be found." );
        }
        
        PlexusRoleListResourceResponse resourceResponse = new PlexusRoleListResourceResponse();
        for ( PlexusRole role : roles )
        {
            resourceResponse.addData( this.nexusToRestModel( role ) );
        }

        return resourceResponse;
    }

    protected String getSourceId( Request request )
    {
        return request.getAttributes().get( SOURCE_ID_KEY ).toString();
    }
}
