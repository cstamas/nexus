package org.sonatype.jsecurity.realms;

import org.codehaus.plexus.PlexusTestCase;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.realm.Realm;

public class MemoryRealmTest
    extends PlexusTestCase
{
    private MemoryRealm realm;
         
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        realm = ( MemoryRealm ) lookup( Realm.class, "MemoryRealm" );
    }
    
    public void testSuccessfulAuthentication()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordToken( "admin", "admin123" );
        
        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );
        
        String password = ( String ) ai.getCredentials();
        
        assertEquals( "admin123", password );        
    }
    
    public void testFailedAuthentication()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordToken( "admin", "badpassword" );
        
        try
        {
            realm.getAuthenticationInfo( upToken );
            
            fail( "Authentication should have failed" );
        }
        catch( AuthenticationException e )
        {
            // good
        }   
    }
    
    public void testAdminAuthorization()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordToken( "admin", "admin123" );
        
        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );
        
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:status:read" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:authentication:read" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:settings:read" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:settings:update" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:repositories:create" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:repositories:read" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:repositories:update" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:repositories:delete" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:target:1:somerepo:read" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:target:1:somerepo:create" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:target:1:somerepo:delete" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:target:1:somerepo:update" ) ) );
        
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "junk" ) ) );
    }
    
    public void testAnonymousAuthorization()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordToken( "anonymous", "anonymous" );
        
        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );
        
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:status:read" ) ) );
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:authentication:read" ) ) );
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:settings:read" ) ) );
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:settings:update" ) ) );
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:repositories:create" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:repositories:read" ) ) );
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:repositories:update" ) ) );
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:repositories:delete" ) ) );        
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "junk" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:target:1:somerepo:read" ) ) );
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:target:1:somerepo:create" ) ) );
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:target:1:somerepo:delete" ) ) );
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:target:1:somerepo:update" ) ) );
    }
    
    public void testDeploymentAuthorization()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordToken( "deployment", "deployment123" );
        
        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );
        
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:status:read" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:authentication:read" ) ) );
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:settings:read" ) ) );
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:settings:update" ) ) );
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:repositories:create" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:repositories:read" ) ) );
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:repositories:update" ) ) );
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:repositories:delete" ) ) );        
        assertFalse( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "junk" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:target:1:somerepo:read" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:target:1:somerepo:create" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:target:1:somerepo:delete" ) ) );
        assertTrue( realm.isPermitted( ai.getPrincipals(), new WildcardPermission( "nexus:target:1:somerepo:update" ) ) );
    }
}
