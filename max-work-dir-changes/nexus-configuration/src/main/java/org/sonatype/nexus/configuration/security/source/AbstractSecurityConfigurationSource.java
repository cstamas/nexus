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
package org.sonatype.nexus.configuration.security.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.codehaus.plexus.interpolation.InterpolatorFilterReader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.security.model.Configuration;
import org.sonatype.nexus.configuration.security.model.io.xpp3.NexusSecurityConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.security.model.io.xpp3.NexusSecurityConfigurationXpp3Writer;
import org.sonatype.nexus.configuration.source.AbstractConfigurationSource;
import org.sonatype.nexus.util.ApplicationInterpolatorProvider;

/**
 * Abstract class that encapsulates Modello model loading and saving with interpolation.
 * 
 * @author cstamas
 */
public abstract class AbstractSecurityConfigurationSource
    extends AbstractConfigurationSource
    implements SecurityConfigurationSource
{

    /**
     * The application interpolation provider.
     * 
     * @plexus.requirement
     */
    private ApplicationInterpolatorProvider interpolatorProvider;

    /** The configuration. */
    private Configuration configuration;

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration( Configuration configuration )
    {
        this.configuration = configuration;
    }

    /**
     * Called by subclasses when loaded configuration is rejected for some reason.
     */
    protected void rejectConfiguration( String message )
    {
        this.configuration = null;

        if ( message != null )
        {
            getLogger().warn( message );
        }
    }

    /**
     * Load configuration.
     * 
     * @param file the file
     * @return the configuration
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings( "unchecked" )
    protected void loadConfiguration( InputStream is )
        throws IOException
    {
        setConfigurationUpgraded( false );

        Reader fr = null;

        try
        {
            NexusSecurityConfigurationXpp3Reader reader = new NexusSecurityConfigurationXpp3Reader();

            fr = new InputStreamReader( is );

            InterpolatorFilterReader ip = new InterpolatorFilterReader( fr, interpolatorProvider.getInterpolator() );

            // read again with interpolation
            configuration = reader.read( ip );
        }
        catch ( XmlPullParserException e )
        {
            rejectConfiguration( "Security configuration file was not loaded, it has the wrong structure." );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "security.xml is broken:", e );
            }
        }
        finally
        {
            if ( fr != null )
            {
                fr.close();
            }
        }

        // check the model version if loaded
        if ( configuration != null && !Configuration.MODEL_VERSION.equals( configuration.getVersion() ) )
        {
            rejectConfiguration( "Security configuration file was loaded but discarded, it has the wrong version number." );
        }

        if ( getConfiguration() != null )
        {
            getLogger().debug( "Configuration loaded succesfully." );
        }
    }

    /**
     * Save configuration.
     * 
     * @param file the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void saveConfiguration( OutputStream os, Configuration configuration )
        throws IOException
    {
        Writer fw = null;
        try
        {
            fw = new OutputStreamWriter( os );

            NexusSecurityConfigurationXpp3Writer writer = new NexusSecurityConfigurationXpp3Writer();

            writer.write( fw, configuration );
        }
        finally
        {
            if ( fw != null )
            {
                fw.flush();

                fw.close();
            }
        }
    }

    /**
     * Returns the default source of ConfigurationSource. May be null.
     */
    public SecurityConfigurationSource getDefaultsSource()
    {
        return null;
    }

}
