package org.sonatype.nexus.integrationtests.nexus156;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.RoleResource;

public class Nexus156RolesPermissionTests extends AbstractPrivilegeTest
{
    
    @Test
    public void testCreatePermission()
        throws IOException
    {
        RoleResource role = new RoleResource();

        role.setDescription( "testCreatePermission" );
        role.setName( "testCreatePermission" );
        role.setSessionTimeout( 30 );
        role.addPrivilege( "1" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        Response response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "34" );
        

        // now.... it should work...
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );
        

        // read should fail
        response = this.roleUtil.sendMessage( Method.GET, role );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );
        
        // update should fail
        response = this.roleUtil.sendMessage( Method.PUT, role );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );
        
        // delete should fail
        response = this.roleUtil.sendMessage( Method.DELETE, role );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

    }

    @Test
    public void testUpdatePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        RoleResource role = new RoleResource();
        role.setDescription( "testUpdatePermission" );
        role.setName( "testUpdatePermission" );
        role.setSessionTimeout( 30 );
        role.addPrivilege( "1" );

        Response response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update user
        role.setName( "testUpdatePermission2" );
        response = this.roleUtil.sendMessage( Method.PUT, role );
//        System.out.println( "PROBLEM: "+ this.userUtil.getUser( "test-user" ) );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "36" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        
        // update user
        response = this.roleUtil.sendMessage( Method.PUT, role );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // read should fail
        response = this.roleUtil.sendMessage( Method.GET, role );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );
        
        // update should fail
        response = this.roleUtil.sendMessage( Method.POST, role );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );
        
        // delete should fail
        response = this.roleUtil.sendMessage( Method.DELETE, role );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );
        
        
    }
    
    
}
