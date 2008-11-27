package org.sonatype.nexus.rest.roles;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.nexus.rest.model.RoleListResourceResponse;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.model.RoleResourceRequest;
import org.sonatype.nexus.rest.model.RoleResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RoleListPlexusResource" )
public class RoleListPlexusResource
    extends AbstractRolePlexusResource
{

    public RoleListPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new RoleResourceRequest();
    }

    @Override
    public String getResourceUri()
    {
        return "/roles";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:roles]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RoleListResourceResponse result = new RoleListResourceResponse();

        for ( SecurityRole role : getNexusSecurity().listRoles() )
        {
            RoleResource res = nexusToRestModel( role, request );

            if ( res != null )
            {
                result.addData( res );
            }
        }

        return result;
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RoleResourceRequest resourceRequest = (RoleResourceRequest) payload;
        RoleResourceResponse result = null;

        if ( resourceRequest != null )
        {
            RoleResource resource = resourceRequest.getData();

            SecurityRole role = restToNexusModel( null, resource );

            try
            {
                getNexusSecurity().createRole( role );

                result = new RoleResourceResponse();

                resource.setId( role.getId() );

                resource.setResourceURI( createChildReference( request, resource.getId() ).toString() );

                result.setData( resource );
            }
            catch ( InvalidConfigurationException e )
            {
                // build and throw exception
                handleInvalidConfigurationException( e );
            }
        }
        return result;
    }

}
