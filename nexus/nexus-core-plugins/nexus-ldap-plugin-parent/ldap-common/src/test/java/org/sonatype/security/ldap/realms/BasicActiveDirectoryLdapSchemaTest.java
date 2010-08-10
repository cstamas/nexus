/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms;

import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.sonatype.security.ldap.dao.LdapAuthConfiguration;
import org.sonatype.security.ldap.dao.LdapGroupDAO;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.dao.LdapUserDAO;
import org.sonatype.security.ldap.dao.NoSuchLdapUserException;
import org.sonatype.security.ldap.realms.persist.LdapConfiguration;


public class BasicActiveDirectoryLdapSchemaTest
    extends PlexusTestCase
{

    private LdapConfiguration ldapConfiguration;

    private LdapGroupDAO ldapGroupManager;

    private LdapUserDAO ldapUserManager;

    private LdapContextFactory ldapContextFactory;

    private Realm realm;

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        String classname = this.getClass().getName();
        context.put( "test-path", getBasedir() + "/target/test-classes/" + classname.replace( '.', '/' ) );
    }

    /*
     * (non-Javadoc)
     * @see org.sonatype.ldaptestsuite.AbstractLdapTestEnvironment#setUp()
     */
    @Override
    public void setUp()
        throws Exception
    {
        // configure the logging
        // SLF4JBridgeHandler.install();

        super.setUp();

        this.ldapGroupManager = this.lookup( LdapGroupDAO.class );
        this.ldapConfiguration = this.lookup( LdapConfiguration.class );
        
        // FIXME: this test is not autmated, and now it is BROKEN, but this needs bo be resolved, as the PlexusLdapContextFactory has been removed
        
//        this.ldapContextFactory = this.lookup( LdapContextFactory.class, "PlexusLdapContextFactory" );
        this.ldapUserManager = lookup( LdapUserDAO.class );
        this.realm = this.lookup( Realm.class, "LdapAuthenticatingRealm" );
    }

    public void testUserManager()
        throws Exception
    {
        LdapAuthConfiguration configuration = this.ldapConfiguration.getLdapAuthConfiguration();

        LdapUser user =
            this.ldapUserManager.getUser( "jcoder", this.ldapContextFactory.getSystemLdapContext(), configuration );
        assertEquals( "jcoder", user.getUsername() );
        assertEquals( "Joe Coder", user.getRealName() );

        try
        {
            user =
                this.ldapUserManager.getUser( "intruder", this.ldapContextFactory.getSystemLdapContext(), configuration );
            fail( "Expected NoSuchUserException" );
        }
        catch ( NoSuchLdapUserException e )
        {
            // good
        }
    }

    public void testGroupManager()
        throws Exception
    {
        LdapAuthConfiguration configuration = ldapConfiguration.getLdapAuthConfiguration();

        Set<String> groups =
            this.ldapGroupManager.getGroupMembership( "jcoder", this.ldapContextFactory.getSystemLdapContext(),
                                                      configuration );

        assertTrue( groups.contains( "Users" ) );
        assertTrue( groups.contains( "Backup Operators" ) );
        assertTrue( groups.contains( "Pre-Windows 2000 Compatible Access" ) );
        assertTrue( groups.contains( "Schema Admins" ) );
    }

    public void testSuccessfulAuthentication()
        throws Exception
    {

        UsernamePasswordToken upToken = new UsernamePasswordToken( "jcoder", "Jpass123" );

        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );

        assertNull( ai.getCredentials() );

        // String password = new String( (char[]) ai.getCredentials() );
        //
        // // password is plain text
        // assertEquals( "brianf123", password );
    }

    public void testWrongPassword()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordToken( "jcoder", "JUNK" );
        try
        {
            realm.getAuthenticationInfo( upToken );
            Assert.fail( "Expected AuthenticationException exception." );
        }
        catch ( AuthenticationException e )
        {
            // expected
        }
    }

    public void testFailedAuthentication()
    {

        UsernamePasswordToken upToken = new UsernamePasswordToken( "username", "password" );
        try
        {
            realm.getAuthenticationInfo( upToken );
            Assert.fail( "Expected AuthenticationException exception." );
        }
        catch ( AuthenticationException e )
        {
            // expected
        }
    }

    protected boolean isPasswordsEncrypted()
    {
        return false;
    }

}
