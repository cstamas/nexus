/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus;

import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.IOUtil;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.sonatype.nexus.security.ldap.realms.NexusLdapAuthenticationRealm;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.realms.XmlAuthenticatingRealm;

public class MultipleRealmsTest
    extends AbstractNexusTestCase
{

    
    public void testAuthentication() throws Exception
    {   
        SecuritySystem security = lookup( SecuritySystem.class );
        security.start();
       
       security.authenticate( new UsernamePasswordToken( "cstamas", "cstamas123" ) );
       
       security.authenticate( new UsernamePasswordToken( "admin", "admin123" ) );
       
       security.authenticate( new UsernamePasswordToken( "deployment", "deployment123" ) );
    }
    
    public void testAuthorization() throws Exception
    {   
        SecuritySystem security = lookup( SecuritySystem.class );
        security.start();
       
       // LDAP user
       SimplePrincipalCollection principals = new SimplePrincipalCollection();
       principals.add( "cstamas", new NexusLdapAuthenticationRealm().getName() );
       
       Assert.assertTrue( security.hasRole( principals, "developer" ) );
       Assert.assertFalse( security.hasRole( principals, "JUNK" ) );
       
       // xml user
       principals = new SimplePrincipalCollection();
       // users must be from the correct realm now!
       principals.add( "deployment", new XmlAuthenticatingRealm().getName() );
       
       Assert.assertTrue( security.hasRole( principals, "deployment" ) );
       Assert.assertFalse( security.hasRole( principals, "JUNK" ) );
       
    }
    
    public void testAuthorizationPriv() throws Exception
    {  
        SecuritySystem security = lookup( SecuritySystem.class );
        security.start();
       
       // LDAP
       SimplePrincipalCollection principals = new SimplePrincipalCollection();
       principals.add( "cstamas", new NexusLdapAuthenticationRealm().getName() );
       
       Assert.assertTrue( security.isPermitted( principals, "security:usersforgotpw:create" ) );
       Assert.assertFalse( security.isPermitted( principals, "security:usersforgotpw:delete" ) );
       
       // XML
       principals = new SimplePrincipalCollection();
       principals.add( "test-user", new XmlAuthenticatingRealm().getName() );
       
       Assert.assertTrue( security.isPermitted( principals, "security:usersforgotpw:create" ) );
       Assert.assertFalse( security.isPermitted( principals, "security:usersforgotpw:delete" ) );
       
       Assert.assertTrue( security.isPermitted( principals, "nexus:target:1:*:delete" ) );
       
       
    }
    
    
    protected void copyDefaultConfigToPlace()
        throws IOException
    {
        IOUtil.copy( getClass().getResourceAsStream( "/test-conf/security-configuration-multipleRealms.xml" ), new FileOutputStream(
            getSecurityConfiguration() ) );
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sonatype.nexus.AbstractNexusTestCase#customizeContext(org.codehaus.plexus.context.Context)
     */
    @Override
    protected void customizeContext( Context ctx )
    {
        super.customizeContext( ctx );

        ctx.put( LDAP_CONFIGURATION_KEY, CONF_HOME.getAbsolutePath() );
    }

}
