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
package org.sonatype.nexus.integrationtests.nexus511;

import java.io.File;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.sonatype.nexus.integrationtests.TestContainer;

/**
 * Tests deploy to nexus using mvn deploy
 */
public class Nexus511MavenDeployIT
    extends AbstractMavenNexusIT
{

    static
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    private Verifier verifier;

    @Before
    public void createVerifier()
        throws Exception
    {
        File mavenProject = getTestFile( "maven-project" );
        File settings = getTestFile( "server.xml" );
        verifier = createVerifier( mavenProject, settings );
    }

    @Test
    public void deploy()
        throws Exception
    {
        try
        {
            verifier.executeGoal( "deploy" );
            verifier.verifyErrorFreeLog();
        }
        catch ( VerificationException e )
        {
            failTest( verifier );
        }
    }

    @Test
    public void privateDeploy()
        throws Exception
    {
        // try to deploy without servers authentication tokens
        File mavenProject = getTestFile( "maven-project" );
        File settings = getTestFile( "serverWithoutAuthentication.xml" );
        verifier = createVerifier( mavenProject, settings );

        try
        {
            verifier.executeGoal( "deploy" );
            verifier.verifyErrorFreeLog();
            failTest( verifier );
        }
        catch ( VerificationException e )
        {
            // Expected exception
        }
    }

}
