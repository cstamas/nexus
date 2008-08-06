package org.sonatype.nexus.integrationtests.nexus408;

import org.junit.Assert;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.UserChangePasswordRequest;
import org.sonatype.nexus.rest.model.UserChangePasswordResource;
import org.sonatype.nexus.rest.model.UserForgotPasswordRequest;
import org.sonatype.nexus.rest.model.UserForgotPasswordResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class ChangePasswordUtils
{

    private static XStream xstream;

    static
    {
        xstream = new XStream();
        XStreamInitializer.initialize( xstream );
    }

    public static Response recoverUsername( String username, String oldPassword, String newPassword )
        throws Exception
    {
        String serviceURI = "service/local/users/changepw/";

        UserChangePasswordResource resource = new UserChangePasswordResource();
        resource.setUserId( username );
        resource.setOldPassword( oldPassword );
        resource.setNewPassword( newPassword );

        UserChangePasswordRequest request = new UserChangePasswordRequest();
        request.setData( resource );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", MediaType.APPLICATION_XML );
        representation.setPayload( request );

        return RequestFacade.sendMessage( serviceURI, Method.POST, representation );

    }

}
