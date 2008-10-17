package org.sonatype.nexus.integrationtests.nexus586;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.nexus393.ResetPasswordUtils;


/**
 * Saving the Nexus config needs to validate the anonymous user information 
 */
public class Nexus586AnonymousResetPasswordTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void resetPassword()
        throws Exception
    {
        String username = "anonymous";
        Response response = ResetPasswordUtils.resetPassword( username );
        Assert.assertEquals( 400, response.getStatus().getCode() );
    }
}
