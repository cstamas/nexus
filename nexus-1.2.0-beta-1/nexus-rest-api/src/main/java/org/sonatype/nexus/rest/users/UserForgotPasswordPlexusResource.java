package org.sonatype.nexus.rest.users;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;
import org.sonatype.nexus.jsecurity.NoSuchEmailException;
import org.sonatype.nexus.rest.model.UserForgotPasswordRequest;
import org.sonatype.nexus.rest.model.UserForgotPasswordResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "UserForgotPasswordPlexusResource" )
public class UserForgotPasswordPlexusResource
    extends AbstractUserPlexusResource
{

    public UserForgotPasswordPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new UserForgotPasswordRequest();
    }

    @Override
    public String getResourceUri()
    {
        return "/users_forgotpw";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:usersforgotpw]" );
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        UserForgotPasswordRequest forgotPasswordRequest = (UserForgotPasswordRequest) payload;

        if ( forgotPasswordRequest != null )
        {
            UserForgotPasswordResource resource = forgotPasswordRequest.getData();

            try
            {
                if ( !isAnonymousUser( resource.getUserId(), request ) )
                {
                    getNexusSecurity().forgotPassword( resource.getUserId(), resource.getEmail() );

                    response.setStatus( Status.SUCCESS_ACCEPTED );
                }
                else
                {
                    response.setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Anonymous user cannot forget password" );

                    getLogger().debug( "Anonymous user forgot password is blocked" );
                }
            }
            catch ( NoSuchUserException e )
            {
                getLogger().debug( "Invalid Username", e );

                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid Username" );
            }
            catch ( NoSuchEmailException e )
            {
                getLogger().debug( "Invalid E-mail", e );

                response.setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "E-mail address not found" );
            }
        }
        // return null because the status is 202
        return null;
    }

}
