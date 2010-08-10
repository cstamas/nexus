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
package org.sonatype.security.realms;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.security.realms.MemoryAuthenticationOnlyRealm;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;

public class MemoryAuthenticationOnlyRealmTest
    extends PlexusTestCase
{
    private MemoryAuthenticationOnlyRealm realm;
         
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        realm = ( MemoryAuthenticationOnlyRealm ) lookup( Realm.class, "MemoryAuthenticationOnlyRealm" );
    }
    
    public void testSuccessfulAuthentication()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordToken( "admin", "admin321" );
        
        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );
        
        String password = ( String ) ai.getCredentials();
        
        assertEquals( "admin321", password );        
    }
    
    public void testFailedAuthentication()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordToken( "admin", "admin123" );
        
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
}
