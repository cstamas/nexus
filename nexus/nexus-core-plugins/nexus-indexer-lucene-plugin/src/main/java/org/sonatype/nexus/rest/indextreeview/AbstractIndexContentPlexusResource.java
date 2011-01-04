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
package org.sonatype.nexus.rest.indextreeview;

import java.io.IOException;
import java.util.HashMap;

import org.apache.maven.index.Field;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.treeview.TreeNode;
import org.apache.maven.index.treeview.TreeNodeFactory;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.AbstractIndexerNexusPlexusResource;
import org.sonatype.nexus.rest.NoSuchRepositoryAccessException;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

import com.thoughtworks.xstream.XStream;

/**
 * Abstract index content resource.
 * 
 * @author dip
 */
public abstract class AbstractIndexContentPlexusResource
    extends AbstractIndexerNexusPlexusResource
{
    private static final String HINT_GROUP_ID = "groupIdHint";

    private static final String HINT_ARTIFACT_ID = "artifactIdHint";

    private static final String HINT_VERSION = "versionHint";

    @Requirement
    protected IndexerManager indexerManager;

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public void configureXStream( XStream xstream )
    {
        super.configureXStream( xstream );

        xstream.processAnnotations( IndexBrowserTreeNode.class );
        xstream.processAnnotations( IndexBrowserTreeViewResponseDTO.class );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String path = parsePathFromUri( request.getResourceRef().toString() );
        if ( !path.endsWith( "/" ) )
        {
            response.redirectPermanent( path + "/" );
            return null;
        }

        String groupIdHint = null;
        String artifactIdHint = null;
        String versionHint = null;

        Form form = request.getResourceRef().getQueryAsForm();

        if ( form.getFirstValue( HINT_GROUP_ID ) != null )
        {
            groupIdHint = form.getFirstValue( HINT_GROUP_ID );
        }
        if ( form.getFirstValue( HINT_ARTIFACT_ID ) != null )
        {
            artifactIdHint = form.getFirstValue( HINT_ARTIFACT_ID );
        }
        if ( form.getFirstValue( HINT_VERSION ) != null )
        {
            versionHint = form.getFirstValue( HINT_VERSION );
        }

        String repositoryId = getRepositoryId( request );

        try
        {
            Repository repository = getUnprotectedRepositoryRegistry().getRepository( repositoryId );

            if ( GroupRepository.class.isInstance( repository ) || repository.isSearchable() )
            {
                TreeNodeFactory factory =
                    new IndexBrowserTreeNodeFactory( repository, createRedirectBaseRef( request ).toString() );

                HashMap<Field, String> hints = new HashMap<Field, String>();

                if ( StringUtils.isNotBlank( groupIdHint ) )
                {
                    hints.put( MAVEN.GROUP_ID, groupIdHint );
                }

                if ( StringUtils.isNotBlank( artifactIdHint ) )
                {
                    hints.put( MAVEN.ARTIFACT_ID, artifactIdHint );
                }

                if ( StringUtils.isNotBlank( versionHint ) )
                {
                    hints.put( MAVEN.VERSION, versionHint );
                }

                TreeNode node = indexerManager.listNodes( factory, path, hints, null, repository.getId() );

                if ( node == null )
                {
                    throw new PlexusResourceException( Status.CLIENT_ERROR_NOT_FOUND,
                        "Unable to retrieve index tree nodes" );
                }

                return new IndexBrowserTreeViewResponseDTO( (IndexBrowserTreeNode) node );
            }
            else
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND,
                    "The index is disabled for this repository." );
            }
        }
        catch ( NoSuchRepositoryAccessException e )
        {
            getLogger().warn( "Repository access denied, id=" + repositoryId );
            throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN, "Access Denied to Repository" );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().error( "Repository Not Found, id=" + repositoryId, e );
            throw new PlexusResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found", e );
        }
        catch ( IOException e )
        {
            getLogger().error( "Got IO exception while executing treeView, id=" + repositoryId, e );
            throw new PlexusResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage(), e );
        }
    }

    protected abstract String getRepositoryId( Request request );

    protected String parsePathFromUri( String parsedPath )
    {
        // get rid of query part
        if ( parsedPath.contains( "?" ) )
        {
            parsedPath = parsedPath.substring( 0, parsedPath.indexOf( '?' ) );
        }

        // get rid of reference part
        if ( parsedPath.contains( "#" ) )
        {
            parsedPath = parsedPath.substring( 0, parsedPath.indexOf( '#' ) );
        }

        if ( StringUtils.isEmpty( parsedPath ) )
        {
            parsedPath = "/";
        }

        int index = parsedPath.indexOf( "index_content" );

        if ( index > -1 )
        {
            parsedPath = parsedPath.substring( index + "index_content".length() );
        }

        return parsedPath;
    }
}
