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
package org.sonatype.nexus.test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.io.xpp3.SecurityConfigurationXpp3Reader;
import org.sonatype.security.rest.model.PrivilegeProperty;
import org.sonatype.security.rest.model.PrivilegeStatusResource;
import org.sonatype.security.rest.model.RoleResource;
import org.sonatype.security.rest.model.UserResource;

import com.thoughtworks.xstream.XStream;

public class SecurityConfigUtil
{
    public static void verifyRole( RoleResource role )
        throws IOException
    {
        List<RoleResource> roles = new ArrayList<RoleResource>();
        roles.add( role );
        verifyRolesExistInCore( roles );
    }

    /**
     * Verify the list of roles contains all roles configured in security.xml
     * 
     * @param roles
     */
    public static void verifyRolesComplete( List<RoleResource> roles )
        throws IOException
    {
        for ( CRole cRole : getSecurityConfig().getRoles() )
        {
            RoleResource roleResource = getRoleResource( cRole.getId(), roles );

            Assert.assertNotNull( "Role '" + cRole.getId() + "' should be contained!", roleResource );

            CRole role = RoleConverter.toCRole( roleResource );

            assertRoleEquals( cRole, role );
        }
    }

    public static void assertRoleEquals( CRole roleA, CRole roleB )
    {
        XStream xStream = new XStream();
        String roleStringA = xStream.toXML( roleA );
        String roleStringB = xStream.toXML( roleB );

        Assert.assertTrue( "Role A:\n" + roleStringB + "\nRole B:\n" + roleStringA, new RoleComparator().compare(
            roleA,
            roleB ) == 0 );
    }

    private static RoleResource getRoleResource( String id, List<RoleResource> roles )
    {
        for ( RoleResource role : roles )
        {
            if ( id.equals( role.getId() ) )
            {
                return role;
            }
        }

        return null;
    }

    public static void verifyRolesExistInCore( List<RoleResource> roles )
        throws IOException
    {
        for ( RoleResource roleResource : roles )
        {
            CRole secRole = getCRole( roleResource.getId() );
            Assert.assertNotNull( secRole );
            CRole role = RoleConverter.toCRole( roleResource );

            assertRoleEquals( secRole, role );
        }
    }

    public static void verifyUser( UserResource user )
        throws IOException
    {
        List<UserResource> users = new ArrayList<UserResource>();
        users.add( user );
        verifyUsers( users );
    }

    public static void verifyUsers( List<UserResource> users )
        throws IOException
    {

        for ( Iterator<UserResource> outterIter = users.iterator(); outterIter.hasNext(); )
        {
            UserResource userResource = outterIter.next();

            CUser secUser = getCUser( userResource.getUserId() );

            Assert.assertNotNull( "Cannot find user: " + userResource.getUserId(), secUser );

            CUser user = UserConverter.toCUser( userResource );

            Assert.assertTrue( new UserComparator().compare( user, secUser ) == 0 );

        }
    }

    public static String getPrivilegeProperty( PrivilegeStatusResource priv, String key )
    {
        for ( PrivilegeProperty prop : (List<PrivilegeProperty>) priv.getProperties() )
        {
            if ( prop.getKey().equals( key ) )
            {
                return prop.getValue();
            }
        }

        return null;
    }

    public static void verifyPrivileges( List<PrivilegeStatusResource> privs )
        throws IOException
    {
        for ( Iterator<PrivilegeStatusResource> iter = privs.iterator(); iter.hasNext(); )
        {
            PrivilegeStatusResource privResource = iter.next();

            CPrivilege secPriv = getCPrivilege( privResource.getId() );

            Assert.assertNotNull( secPriv );

            Assert.assertEquals( privResource.getId(), secPriv.getId() );
            Assert.assertEquals( privResource.getName(), secPriv.getName() );
            Assert.assertEquals( privResource.getDescription(), secPriv.getDescription() );

            for ( CProperty prop : (List<CProperty>) secPriv.getProperties() )
            {
                Assert.assertEquals( getPrivilegeProperty( privResource, prop.getKey() ), prop.getValue() );
            }
        }
    }

    public static CRole getCRole( String roleId )
        throws IOException
    {
        Configuration securityConfig = getSecurityConfig();
        List<CRole> secRoles = securityConfig.getRoles();

        for ( Iterator<CRole> iter = secRoles.iterator(); iter.hasNext(); )
        {
            CRole cRole = iter.next();

            if ( roleId.equals( cRole.getId() ) )
            {
                return cRole;
            }
        }
        return null;
    }

    public static CPrivilege getCPrivilege( String privilegeId )
        throws IOException
    {
        Configuration securityConfig = getSecurityConfig();
        List<CPrivilege> secPrivs = securityConfig.getPrivileges();

        for ( Iterator<CPrivilege> iter = secPrivs.iterator(); iter.hasNext(); )
        {
            CPrivilege cPriv = iter.next();

            if ( privilegeId.equals( cPriv.getId() ) )
            {
                return cPriv;
            }
        }
        return null;
    }

    public static CPrivilege getCPrivilegeByName( String privilegeName )
        throws IOException
    {
        Configuration securityConfig = getSecurityConfig();
        List<CPrivilege> secPrivs = securityConfig.getPrivileges();

        for ( Iterator<CPrivilege> iter = secPrivs.iterator(); iter.hasNext(); )
        {
            CPrivilege cPriv = iter.next();

            if ( privilegeName.equals( cPriv.getName() ) )
            {
                return cPriv;
            }
        }
        return null;
    }

    public static CUser getCUser( String userId )
        throws IOException
    {
        Configuration securityConfig = getSecurityConfig();
        List<CUser> secUsers = securityConfig.getUsers();

        for ( Iterator<CUser> iter = secUsers.iterator(); iter.hasNext(); )
        {
            CUser cUser = iter.next();

            if ( userId.equals( cUser.getId() ) )
            {
                return cUser;
            }
        }
        return null;
    }

    public static Configuration getSecurityConfig()
        throws IOException
    {
        File secConfigFile = new File( AbstractNexusIntegrationTest.WORK_CONF_DIR, "security.xml" );

        Reader fr = null;
        Configuration configuration = null;

        try
        {
            SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();

            fr = new InputStreamReader( new FileInputStream( secConfigFile ) );

            // read again with interpolation
            try
            {
                configuration = reader.read( fr );
            }
            finally
            {
                fr.close();
            }

            Configuration staticConfiguration = null;

            fr = new InputStreamReader( SecurityConfigUtil.class
                .getResourceAsStream( "/META-INF/nexus/static-security.xml" ) );

            try
            {
                staticConfiguration = reader.read( fr );
            }
            finally
            {
                fr.close();
            }

            for ( CUser user : (List<CUser>) staticConfiguration.getUsers() )
            {
                configuration.addUser( user );
            }
            for ( CRole role : (List<CRole>) staticConfiguration.getRoles() )
            {
                configuration.addRole( role );
            }
            for ( CPrivilege priv : (List<CPrivilege>) staticConfiguration.getPrivileges() )
            {
                configuration.addPrivilege( priv );
            }

        }
        catch ( XmlPullParserException e )
        {
            Assert.fail( "could not parse nexus.xml: " + e.getMessage() );
        }
        return configuration;
    }

}
