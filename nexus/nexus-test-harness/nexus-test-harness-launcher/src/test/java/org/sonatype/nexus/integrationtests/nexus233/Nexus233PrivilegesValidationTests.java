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
package org.sonatype.nexus.integrationtests.nexus233;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.jsecurity.realms.privileges.application.ApplicationPrivilegeDescriptor;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.test.utils.PrivilegesMessageUtil;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;
import org.sonatype.security.rest.model.PrivilegeResource;

/**
 * Extra CRUD validation tests.
 */
public class Nexus233PrivilegesValidationTests
    extends AbstractNexusIntegrationTest
{

    protected PrivilegesMessageUtil messageUtil;

    public Nexus233PrivilegesValidationTests()
    {
        this.messageUtil =
            new PrivilegesMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createWithInvalidMethodTest()
        throws IOException
    {
        PrivilegeResource resource = new PrivilegeResource();

        List methods = new ArrayList<String>();
        methods.add( "INVALID" );
        resource.setMethod( methods );
        resource.setName( "createWithInvalidMethodTest" );
        resource.setType( TargetPrivilegeDescriptor.TYPE );
        resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Privilege should not have been created: " + response.getStatus() + "\nreponse:\n"
                + responseText );
        }
        this.messageUtil.validateResponseErrorXml( responseText );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createNoMethodTest()
        throws IOException
    {
        PrivilegeResource resource = new PrivilegeResource();

        List methods = new ArrayList<String>();
        // methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "createNoMethodTest" );
        resource.setType( TargetPrivilegeDescriptor.TYPE );
        resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Privilege should not have been created: " + response.getStatus() + "\nreponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createNoNameTest()
        throws IOException
    {
        PrivilegeResource resource = new PrivilegeResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        // resource.setName( "createNoMethodTest" );
        resource.setType( TargetPrivilegeDescriptor.TYPE );
        resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Privilege should not have been created: " + response.getStatus() + "\nreponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createNoTypeTest()
        throws IOException
    {
        PrivilegeResource resource = new PrivilegeResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "createNoTypeTest" );
        // resource.setType( "target" );
//        resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        
        if ( response.getStatus().isSuccess())
        {
            Assert.fail( "No type, POST should've failed");
        }
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createNoRepoTest()
        throws IOException
    {
        PrivilegeResource resource = new PrivilegeResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "createNoRepoTest" );
        resource.setType( TargetPrivilegeDescriptor.TYPE );
        // resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Privilege should not have been created: " + response.getStatus() + "\nreponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createWithInvalidAndValidMethodsTest()
        throws IOException
    {
        PrivilegeResource resource = new PrivilegeResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        methods.add( "INVALID" );
        resource.setMethod( methods );
        resource.setName( "createWithInvalidAndValidMethodsTest" );
        resource.setType( TargetPrivilegeDescriptor.TYPE );
        // resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Privilege should not have been created: " + response.getStatus() + "\nreponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );

        Assert.assertNull( SecurityConfigUtil.getCPrivilegeByName( "createWithInvalidAndValidMethodsTest - (read)" ) );
    }

    @Test
    public void createApplicationResource()
        throws IOException
    {
        PrivilegeResource resource = new PrivilegeResource();
        resource.addMethod( "read" );
        resource.setName( "createApplicationResource" );
        resource.setType( ApplicationPrivilegeDescriptor.TYPE );
        //resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Privilege should not have been created: " + response.getStatus() + "\nreponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );
    }

    @Test
    public void readInvalidIdTest()
        throws IOException
    {

        Response response = this.messageUtil.sendMessage( Method.GET, null, "INVALID" );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 404 )
        {
            Assert.fail( "A 404 should have been returned: " + response.getStatus() + "\nreponse:\n" + responseText );
        }

    }

    @Test
    public void deleteInvalidIdTest()
        throws IOException
    {

        Response response = this.messageUtil.sendMessage( Method.DELETE, null, "INVALID" );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 404 )
        {
            Assert.fail( "A 404 should have been returned: " + response.getStatus() + "\nreponse:\n" + responseText );
        }

    }

}
