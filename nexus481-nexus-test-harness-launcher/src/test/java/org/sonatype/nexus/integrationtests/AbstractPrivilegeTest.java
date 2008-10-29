package org.sonatype.nexus.integrationtests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.restlet.data.MediaType;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.PrivilegesMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.RoleMessageUtil;
import org.sonatype.nexus.test.utils.RoutesMessageUtil;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;
import org.sonatype.nexus.test.utils.TargetMessageUtil;
import org.sonatype.nexus.test.utils.UserMessageUtil;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import com.thoughtworks.xstream.XStream;

public abstract class AbstractPrivilegeTest
    extends AbstractNexusIntegrationTest
{

    protected static final String TEST_USER_NAME = "test-user";

    protected static final String TEST_USER_PASSWORD = "admin123";

    protected UserMessageUtil userUtil;

    protected RoleMessageUtil roleUtil;

    protected PrivilegesMessageUtil privUtil;

    protected TargetMessageUtil targetUtil;

    protected RoutesMessageUtil routeUtil;
    
    protected RepositoryMessageUtil repoUtil;
    
    protected GroupMessageUtil groupUtil;

    public AbstractPrivilegeTest( String testRepositoryId)
    {
      super( testRepositoryId );
      this.init();
    }

    public AbstractPrivilegeTest()
    {
        this.init();
    }

    private void init()
    {
        // turn on security for the test
        TestContainer.getInstance().getTestContext().setSecureTest( true );
        
        XStream xstream = this.getXMLXStream();

        this.userUtil = new UserMessageUtil( xstream, MediaType.APPLICATION_XML );
        this.roleUtil = new RoleMessageUtil( xstream, MediaType.APPLICATION_XML );
        this.privUtil =
            new PrivilegesMessageUtil( xstream, MediaType.APPLICATION_XML );
        this.targetUtil =
            new TargetMessageUtil( xstream, MediaType.APPLICATION_XML );
        TestContainer.getInstance().getTestContext().setSecureTest( true );
        this.routeUtil =
            new RoutesMessageUtil( xstream, MediaType.APPLICATION_XML );
        this.repoUtil = new RepositoryMessageUtil( xstream, MediaType.APPLICATION_XML );
        this.groupUtil = new GroupMessageUtil( xstream, MediaType.APPLICATION_XML );
    }




    @BeforeTest
    public void resetTestUserPrivs()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        UserResource testUser = this.userUtil.getUser( TEST_USER_NAME );
        testUser.getRoles().clear();
        testUser.addRole( "anonymous" );
        this.userUtil.updateUser( testUser );
    }

    protected void printUserPrivs( String userId )
        throws IOException
    {
        UserResource user = this.userUtil.getUser( userId );
        ArrayList<String> privs = new ArrayList<String>();

        for ( Iterator iter = user.getRoles().iterator(); iter.hasNext(); )
        {
            String roleId = (String) iter.next();
            RoleResource role = this.roleUtil.getRole( roleId );

            for ( Iterator roleIter = role.getPrivileges().iterator(); roleIter.hasNext(); )
            {
                String privId = (String) roleIter.next();
                // PrivilegeBaseStatusResource priv = this.privUtil.getPrivilegeResource( privId );
                // privs.add( priv.getName() );
                CPrivilege priv = SecurityConfigUtil.getCPrivilege( privId );
                if ( priv != null )
                {
                    privs.add( priv.getName() );
                }
                else
                {
                    PrivilegeBaseStatusResource basePriv = this.privUtil.getPrivilegeResource( privId );
                    privs.add( basePriv.getName() );
                }

            }
        }

        System.out.println( "User: " + userId );
        for ( Iterator iter = privs.iterator(); iter.hasNext(); )
        {
            String privName = (String) iter.next();
            System.out.println( "\t" + privName );
        }
    }

    protected void giveUserPrivilege( String userId, String priv )
        throws IOException
    {
        // use admin
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // now give create
        RoleResource role = new RoleResource();
        role.setDescription( priv + " Role" );
        role.setName( priv + "Role" );
        role.setSessionTimeout( 60 );
        role.addPrivilege( priv );
        // save it
        role = this.roleUtil.createRole( role );

        // add it
        this.giveUserRole( userId, role.getId() );
    }

    protected void giveUserRole( String userId, String roleId ) throws IOException
    {
     // use admin
        TestContainer.getInstance().getTestContext().useAdminForRequests();

     // add it
        UserResource testUser = this.userUtil.getUser( userId );
        testUser.addRole( roleId );
        this.userUtil.updateUser( testUser );
    }


    protected void overwriteUserRole( String userId, String newRoleName, String... permissions )
        throws Exception
    {
        // use admin
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        
        // now give create
        RoleResource role = new RoleResource();
        role.setDescription( newRoleName );
        role.setName( newRoleName );
        role.setSessionTimeout( 60 );
        for ( String priv : permissions )
        {
            role.addPrivilege( priv );
        }
        // save it
        role = this.roleUtil.createRole( role );

        // add it
        UserResource testUser = this.userUtil.getUser( userId );
        testUser.getRoles().clear();
        testUser.addRole( role.getId() );
        this.userUtil.updateUser( testUser );
    }

    @AfterTest
    public void afterTest()
        throws Exception
    {
        // reset any password
        TestContainer.getInstance().getTestContext().useAdminForRequests();
    }
}
