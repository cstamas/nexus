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
package org.sonatype.nexus.rest.artifact;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ArtifactStore;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;

public class AbstractArtifactResourceHandler
    extends AbstractNexusResourceHandler
{
    public AbstractArtifactResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    protected Model getPom()
    {
        Form form = getRequest().getResourceRef().getQueryAsForm();

        String groupId = form.getFirstValue( "g" );

        String artifactId = form.getFirstValue( "a" );

        String version = form.getFirstValue( "v" );

        String repositoryId = form.getFirstValue( "r" );

        if ( groupId == null || artifactId == null || version == null || repositoryId == null )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST );

            return null;
        }

        try
        {
            Repository repository = getNexus().getRepository( repositoryId );

            if ( !ArtifactStore.class.isAssignableFrom( repository.getClass() ) )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "This is not a Maven repository!" );

                return null;
            }

            InputStream pomContent = null;

            InputStreamReader ir = null;

            Model pom = null;

            try
            {
                StorageFileItem file = ( (ArtifactStore) repository )
                    .retrieveArtifactPom( groupId, artifactId, version );

                pomContent = file.getInputStream();

                MavenXpp3Reader reader = new MavenXpp3Reader();

                ir = new InputStreamReader( pomContent );

                pom = reader.read( ir );
            }
            finally
            {
                if ( ir != null )
                {
                    ir.close();
                }
                if ( pomContent != null )
                {
                    pomContent.close();
                }
            }

            return pom;

        }
        catch ( StorageException e )
        {
            getLogger().log( Level.SEVERE, "StorageException during retrieve:", e );

            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );
        }
        catch ( NoSuchResourceStoreException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "No repository with id=" + repositoryId );
        }
        catch ( RepositoryNotAvailableException e )
        {
            getResponse().setStatus( Status.SERVER_ERROR_SERVICE_UNAVAILABLE );
        }
        catch ( ItemNotFoundException e )
        {
            // nothing
        }
        catch ( AccessDeniedException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_FORBIDDEN );
        }
        catch ( XmlPullParserException e )
        {
            getLogger().log( Level.SEVERE, "XmlPullParserException during retrieve of POM:", e );

            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );
        }
        catch ( IOException e )
        {
            getLogger().log( Level.SEVERE, "IOException during retrieve of POM:", e );

            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );
        }

        return null;

    }
}
