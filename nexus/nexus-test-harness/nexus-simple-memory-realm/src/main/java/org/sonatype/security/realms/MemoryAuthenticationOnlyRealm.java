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

import java.util.HashMap;
import java.util.Map;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.codehaus.plexus.component.annotations.Component;

/**
 * This is a sample of how you can inject your own authentication system
 * but still leave the authorization to nexus.
 * 
 * This MemoryAuthenticationOnlyRealm will handle authentication on its own
 * 
 * This class can also be loaded by nexus as either a regular class, or as a 
 * plexus component.  In the nexus.xml file, you can remove the default
 * XmlAuthenticatingRealm and add this realm as either 
 * org.sonatype.jsecurity.realms.MemoryAuthenticationOnlyRealm, or
 * use the role-hint of MemoryAuthenticationOnlyRealm.
 * 
 */
@Component(role=Realm.class, hint="MemoryAuthenticationOnlyRealm")
public class MemoryAuthenticationOnlyRealm
    extends AuthorizingRealm
{
    // Map containing username/password pairs
    private Map<String,String> authenticationMap = new HashMap<String,String>();
        
    /**
     * This is where we are building the security model, not that the passwords have
     * been changed from the default nexus security, to make for easy validation
     */
    public MemoryAuthenticationOnlyRealm()
    {
        // As this is a simple test realm, only using simple credentials
        // just a string compare, no hashing involved
        setCredentialsMatcher( new SimpleCredentialsMatcher() );
        
        authenticationMap.put( "admin", "admin321" );
        authenticationMap.put( "deployment", "deployment321" );
        authenticationMap.put( "anonymous", "anonymous" );
    }

    public String getName()
    {
        return MemoryAuthenticationOnlyRealm.class.getName();
    }
    
    /**
     * This method is where the authentication is controlled.  You will receive a
     * token, from which you can retrieve the username.  Then you can lookup in your
     * storage, the credentials for that user, place those in an AuthenticationInfo
     * object and return it, the credential matcher will handle comparing them.
     * 
     * @see org.jsecurity.realm.AuthenticatingRealm#doGetAuthenticationInfo(org.jsecurity.authc.AuthenticationToken)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken arg0 )
        throws AuthenticationException
    {
        if ( !UsernamePasswordToken.class.isAssignableFrom( arg0.getClass() ) )
        {
            return null;
        }
        
        String username = ( ( UsernamePasswordToken ) arg0 ).getUsername();
        
        String password = authenticationMap.get( username );
        
        if ( password == null )
        {
            throw new AuthenticationException( "Invalid username '" + username + "'");
        }
        
        return new SimpleAuthenticationInfo( username, password, getName() );
    }
    
    /**
     * As this is an authentication only realm, we just return null for authorization
     * 
     * @see org.jsecurity.realm.AuthorizingRealm#doGetAuthorizationInfo(org.jsecurity.subject.PrincipalCollection)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection arg0 )
    {
        return null;
    }
}
