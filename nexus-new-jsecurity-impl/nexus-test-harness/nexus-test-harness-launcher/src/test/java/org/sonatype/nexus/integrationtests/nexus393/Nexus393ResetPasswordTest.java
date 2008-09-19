package org.sonatype.nexus.integrationtests.nexus393;

import javax.mail.internet.MimeMessage;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractEmailServerNexusIT;

import com.icegreen.greenmail.util.GreenMailUtil;


/**
 * Test password reset.  Check if nexus is sending the e-mail. 
 */
public class Nexus393ResetPasswordTest
    extends AbstractEmailServerNexusIT
{

    @Test
    public void resetPassword()
        throws Exception
    {
        String username = "test-user";
        Response response = ResetPasswordUtils.resetPassword( username );
        Assert.assertTrue( "Status: "+ response.getStatus(), response.getStatus().isSuccess() );

        // Need 1 message
        server.waitForIncomingEmail( 1000, 1 );

        MimeMessage[] msgs = server.getReceivedMessages();
        MimeMessage msg = msgs[0];

        String password = null;
        // Sample body: Your password has been reset. Your new password is: c1r6g4p8l7
        String body = GreenMailUtil.getBody( msg );
        
        int index = body.indexOf( "Your new password is: " );
        int passwordStartIndex = index + "Your new password is: ".length();
        if ( index != -1 )
        {
            password = body.substring( passwordStartIndex, body.indexOf( '\n', passwordStartIndex ) ).trim();
            log.debug( "New password:\n" + password );
        }

        Assert.assertNotNull( password );
    }

}
