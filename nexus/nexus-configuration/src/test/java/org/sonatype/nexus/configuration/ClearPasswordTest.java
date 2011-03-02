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
package org.sonatype.nexus.configuration;

import java.io.File;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.source.ApplicationConfigurationSource;

import com.thoughtworks.xstream.XStream;

public class ClearPasswordTest
    extends AbstractNexusTestCase
{
    private ApplicationConfigurationSource getConfigSource()
        throws Exception
    {
        // get the config
        return this.lookup( ApplicationConfigurationSource.class, "file" );
    }

    public void testDefaultConfig()
        throws Exception
    {
        // start with the default nexus config
        this.copyDefaultConfigToPlace();

        this.doTestLogic();
    }

    public void testUpgrade()
        throws Exception
    {
        // copy a conf file that needs to be upgraded to the config dir
        FileUtils.copyURLToFile( Thread.currentThread().getContextClassLoader().getResource(
            "org/sonatype/nexus/configuration/upgrade/nexus-001-1.xml" ), new File( this.getNexusConfiguration() ) );

        this.doTestLogic();
    }

    private void doTestLogic()
        throws Exception
    {
        ApplicationConfigurationSource source = this.getConfigSource();

        Configuration config = source.loadConfiguration();

        // make sure the smtp-password is what we expect
        Assert.assertEquals( "Incorrect SMTP password found in nexus.xml", "smtp-password", config
            .getSmtpConfiguration().getPassword() );

        // set the clear passwords
        String password = "clear-text";

        // smtp
        config.getSmtpConfiguration().setPassword( password );

        // global proxy
        config.setGlobalHttpProxySettings( new CRemoteHttpProxySettings() );
        config.getGlobalHttpProxySettings().setAuthentication( new CRemoteAuthentication() );
        config.getGlobalHttpProxySettings().getAuthentication().setPassword( password );
//        config.getSecurity().setAnonymousPassword( password );
//
//        // anon username
//        config.getSecurity().setAnonymousPassword( password );

        // repo auth pass
        CRepository central = this.getCentralRepo( config );
        central.getRemoteStorage().setAuthentication( new CRemoteAuthentication() );
        central.getRemoteStorage().getAuthentication().setPassword( password );

        // repo proxy pass
        central.getRemoteStorage().setHttpProxySettings( new CRemoteHttpProxySettings() );
        central.getRemoteStorage().getHttpProxySettings().setAuthentication( new CRemoteAuthentication() );
        central.getRemoteStorage().getHttpProxySettings().getAuthentication().setPassword( password );

        // now we need to make the file valid....
        config.getGlobalHttpProxySettings().setProxyPort( 1234 );
        central.getRemoteStorage().getHttpProxySettings().setProxyPort( 1234 );

        // save it
        source.storeConfiguration();

        Assert.assertTrue( "Configuration is corroupt, passwords are encrypted (in memory). ", new XStream().toXML(
            config ).contains( password ) );

        // now get the file and look for the "clear-text"
        String configString = FileUtils.fileRead( this.getNexusConfiguration() );

        Assert.assertFalse( "Clear text password found in nexus.xml:\n" + configString, configString
            .contains( password ) );

        // make sure we do not have the default smtp password either
        Assert.assertFalse( "Old SMTP password found in nexus.xml", configString.contains( "smtp-password" ) );

        // now load it again and make sure the password is clear text
        Configuration newConfig = source.loadConfiguration();
        Assert.assertEquals( password, newConfig.getSmtpConfiguration().getPassword() );
        Assert.assertEquals( password, newConfig.getGlobalHttpProxySettings().getAuthentication().getPassword() );
//        Assert.assertEquals( password, newConfig.getSecurity().getAnonymousPassword() );

        central = this.getCentralRepo( newConfig );
        Assert.assertEquals( password, central.getRemoteStorage().getAuthentication().getPassword() );
        Assert.assertEquals( password, central
            .getRemoteStorage().getHttpProxySettings().getAuthentication().getPassword() );

    }

    private CRepository getCentralRepo( Configuration config )
    {
        for ( CRepository repo : config.getRepositories() )
        {
            if ( repo.getId().equals( "central" ) )
            {
                return repo;
            }
        }
        return null;
    }

}
