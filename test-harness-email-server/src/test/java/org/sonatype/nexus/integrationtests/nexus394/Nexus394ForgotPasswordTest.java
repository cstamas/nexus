package org.sonatype.nexus.integrationtests.nexus394;

import javax.mail.internet.MimeMessage;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractEmailServerNexusIT;

import com.icegreen.greenmail.util.GreenMailUtil;

public class Nexus394ForgotPasswordTest
    extends AbstractEmailServerNexusIT
{

    @Test
    public void recoverUserPassword()
        throws Exception
    {
        String username = "admin";
        ForgotPasswordUtils.recoverUserPassword( username, "nexus-dev@sonatype.org" );

        // Need 1 message
        server.waitForIncomingEmail( 1000, 1 );

        MimeMessage[] msgs = server.getReceivedMessages();

        // Sample body: Your password has been reset. Your new password is: c1r6g4p8l7
        String body = GreenMailUtil.getBody( msgs[0] );

        String password = body.substring( body.lastIndexOf( ' ' ) + 1 );
        System.out.println( "New password:\n" + password );

        Assert.assertNotNull( password );
    }

}
