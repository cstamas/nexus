package org.sonatype.nexus.integrationtests.nexus142;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;
import org.sonatype.nexus.test.utils.UserMessageUtil;
import org.testng.annotations.Test;

/**
 * CRUD tests for JSON request/response.
 */
public class Nexus142UserCrudJsonTests
    extends AbstractNexusIntegrationTest
{

    protected UserMessageUtil messageUtil;
    
    public Nexus142UserCrudJsonTests()
    {
        this.messageUtil = new UserMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }

    @Test
    public void createTest()
        throws IOException
    {

        UserResource resource = new UserResource();

        resource.setName( "Create User" );
        resource.setUserId( "createUser" );
        resource.setStatus( "active" );
        resource.setEmail( "nexus@user.com" );
        resource.addRole( "role1" );

        // this also validates
        this.messageUtil.createUser( resource );
    }
    
    @Test
    public void listTest() throws IOException
    {
        UserResource resource = new UserResource();

        resource.setName( "list Test" );
        resource.setUserId( "listTest" );
        resource.setStatus( "active" );
        resource.setEmail( "listTest@user.com" );
        resource.addRole( "role1" );
        
     // this also validates
        this.messageUtil.createUser( resource );
        
        // now that we have at least one element stored (more from other tests, most likely)
        
        
        // NEED to work around a GET problem with the REST client
        List<UserResource> users = this.messageUtil.getList();
        SecurityConfigUtil.verifyUsers( users );
        
        
    }
    
    public void readTest()
        throws IOException
    {

        UserResource resource = new UserResource();

        resource.setName( "Read User" );
        resource.setUserId( "readUser" );
        resource.setStatus( "active" );
        resource.setEmail( "read@user.com" );
        resource.addRole( "role1" );

     // this also validates
        this.messageUtil.createUser( resource );
        
        
        Response response = this.messageUtil.sendMessage( Method.GET, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not GET Repository Target: " + response.getStatus() );
        }
        
        // get the Resource object
        UserResource responseResource = this.messageUtil.getResourceFromResponse( response );

        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getUserId(), responseResource.getUserId() );
        Assert.assertEquals( "active", responseResource.getStatus() );
        Assert.assertEquals( resource.getEmail(), responseResource.getEmail() );
        Assert.assertEquals( resource.getRoles(), responseResource.getRoles() );
    }
    

    @Test
    public void updateTest()
        throws IOException
    {

        UserResource resource = new UserResource();

        resource.setName( "Update User" );
        resource.setUserId( "updateUser" );
        resource.setStatus( "active" );
        resource.setEmail( "updateUser@user.com" );
        resource.addRole( "role1" );

        this.messageUtil.createUser( resource );

        // update the user
        // TODO: add tests that changes the userId
        resource.setName( "Update UserAgain" );
        resource.setUserId( "updateUser" );
        resource.setStatus( "active" );
        resource.setEmail( "updateUser@user2.com" );
        resource.getRoles().clear();
        resource.addRole( "role2" );

        // this validates
        this.messageUtil.updateUser( resource );

    }

    @Test
    public void deleteTest()
        throws IOException
    {

        UserResource resource = new UserResource();

        resource.setName( "Delete User" );
        resource.setUserId( "deleteUser" );
        resource.setStatus( "active" );
        resource.setEmail( "deleteUser@user.com" );
        resource.addRole( "role2" );

        this.messageUtil.createUser( resource );

        // use the new ID
        Response response = this.messageUtil.sendMessage( Method.DELETE, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not delete User: " + response.getStatus() );
        }

        SecurityConfigUtil.verifyUsers( new ArrayList<UserResource>() );
    }

}
