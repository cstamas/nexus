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
package org.sonatype.nexus.integrationtests.nexus395;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.test.utils.ForgotUsernameUtils;

/**
 * Test forgot username system. Check if nexus is sending the e-mail.
 */
public class Nexus395ForgotUsernameIT
    extends AbstractForgotUserNameIT
{

    @Test
    public void recoverUsername()
        throws Exception
    {
        Status status = ForgotUsernameUtils.recoverUsername( "nexus-dev2@sonatype.org" );

        Assert.assertEquals( Status.SUCCESS_ACCEPTED.getCode(), status.getCode() );

        assertRecoveredUserName( "test-user" );
    }

    @Test
    public void recoverAnonymousUserName()
        throws Exception
    {
        String anonymousEmail = "changeme2@yourcompany.com";

        Status status = ForgotUsernameUtils.recoverUsername( anonymousEmail );

        Assert.assertEquals( Status.CLIENT_ERROR_BAD_REQUEST.getCode(), status.getCode() );
    }
}
