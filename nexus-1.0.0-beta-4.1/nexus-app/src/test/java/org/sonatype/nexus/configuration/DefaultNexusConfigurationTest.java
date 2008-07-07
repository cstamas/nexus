/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.io.InputStreamFacade;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupIdBasedRepositoryRouter;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

public class DefaultNexusConfigurationTest
    extends AbstractNexusTestCase
{

    protected DefaultNexusConfiguration nexusConfiguration;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusConfiguration = (DefaultNexusConfiguration) this.lookup( NexusConfiguration.ROLE );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    public void testSaveConfiguration()
        throws Exception
    {
        nexusConfiguration.loadConfiguration();

        Configuration config = nexusConfiguration.getConfiguration();

        assertEquals( true, config.getSecurity().isEnabled() );

        config.getSecurity().setEnabled( false );

        nexusConfiguration.saveConfiguration();

        nexusConfiguration.loadConfiguration();

        config = nexusConfiguration.getConfiguration();

        assertEquals( false, config.getSecurity().isEnabled() );
    }

    public void testSaveGlobalProxyConfiguration()
        throws Exception
    {
        // default has no Global Proxy, we will set one
        nexusConfiguration.loadConfiguration();

        Configuration config = nexusConfiguration.getConfiguration();

        assertEquals( null, config.getGlobalHttpProxySettings() );

        CRemoteHttpProxySettings settings = new CRemoteHttpProxySettings();

        settings.setProxyHostname( "testhost.proxy.com" );

        settings.setProxyPort( 1234 );

        nexusConfiguration.updateGlobalRemoteHttpProxySettings( settings );

        nexusConfiguration.saveConfiguration();

        // force reload
        nexusConfiguration.loadConfiguration( true );

        config = nexusConfiguration.getConfiguration();

        assertEquals(
            nexusConfiguration.getConfiguration().getGlobalHttpProxySettings().getProxyHostname(),
            ( (DefaultNexusConfiguration) nexusConfiguration )
                .getRemoteStorageContext().getRemoteHttpProxySettings().getProxyHostname() );

        assertEquals(
            nexusConfiguration.getConfiguration().getGlobalHttpProxySettings().getProxyPort(),
            ( (DefaultNexusConfiguration) nexusConfiguration )
                .getRemoteStorageContext().getRemoteHttpProxySettings().getProxyPort() );

    }

    public void testLoadConfiguration()
        throws Exception
    {
        // this will create default config
        nexusConfiguration.loadConfiguration();

        // get it
        Configuration config = nexusConfiguration.getConfiguration();

        // check it for default value
        assertEquals( true, config.getSecurity().isEnabled() );

        // modify it
        config.getSecurity().setEnabled( false );

        // save it
        nexusConfiguration.saveConfiguration();

        // replace it again with default "from behind"
        InputStreamFacade isf = new InputStreamFacade()
        {

            public InputStream getInputStream()
                throws IOException
            {
                return getClass().getResourceAsStream( "/META-INF/nexus/nexus.xml" );
            }

        };
        FileUtils.copyStreamToFile( isf, new File( getNexusConfiguration() ) );

        // force reload
        nexusConfiguration.loadConfiguration( true );

        // get the config
        config = nexusConfiguration.getConfiguration();

        // it again contains default value, coz we overwritten it before
        assertEquals( true, config.getSecurity().isEnabled() );
    }

    public void testGetConfiguration()
        throws Exception
    {
        assertEquals( null, nexusConfiguration.getConfiguration() );

        nexusConfiguration.loadConfiguration();

        assertTrue( nexusConfiguration.getConfiguration() != null );
    }

    public void testGetConfigurationAsStream()
        throws Exception
    {
        nexusConfiguration.loadConfiguration();

        IOUtil.contentEquals( new FileInputStream( new File( getNexusConfiguration() ) ), nexusConfiguration
            .getConfigurationAsStream() );
    }

    public void testGetDefaultConfigurationAsStream()
        throws Exception
    {
        nexusConfiguration.loadConfiguration();

        IOUtil.contentEquals( getClass().getResourceAsStream( "/META-INF/nexus/nexus.xml" ), nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfigurationAsStream() );
    }

    public void testNX467()
        throws Exception
    {
        // load default config
        nexusConfiguration.loadConfiguration();

        M2GroupIdBasedRepositoryRouter groupRouter = (M2GroupIdBasedRepositoryRouter) lookup(
            RepositoryRouter.ROLE,
            "groups-m2" );

        // runtime state should equal to config
        assertEquals( nexusConfiguration.getConfiguration().getRouting().getGroups().isMergeMetadata(), groupRouter
            .isMergeMetadata() );

        // invert runtime state
        groupRouter.setMergeMetadata( !groupRouter.isMergeMetadata() );

        // force reloading of config
        nexusConfiguration.loadConfiguration( true );

        // runtime state should equal to config again
        assertEquals( nexusConfiguration.getConfiguration().getRouting().getGroups().isMergeMetadata(), groupRouter
            .isMergeMetadata() );
    }
}
