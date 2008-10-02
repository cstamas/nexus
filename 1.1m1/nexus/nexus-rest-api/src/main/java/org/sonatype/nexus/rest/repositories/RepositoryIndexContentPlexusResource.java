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
package org.sonatype.nexus.rest.repositories;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchRequest;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.rest.model.ContentListResourceResponse;
import org.sonatype.nexus.rest.model.NexusArtifact;

/**
 * Index content resource.
 * 
 * @author dip
 * @plexus.component role-hint="repoIndexResource"
 */
public class RepositoryIndexContentPlexusResource extends AbstractNexusPlexusResource {

    public static final String REPOSITORY_ID_KEY = "repositoryId";

    /**
     * @plexus.requirement
     */
    private IndexerManager indexerManager;
    
    @Override
    public Object getPayloadInstance() {
        return null;
    }

    @Override
    public String getResourceUri() {
        return "/repositories/{" + REPOSITORY_ID_KEY + "}/index_content";
    }
    
    @Override
    public Object get(Context context, Request request, Response response, Variant variant)
            throws ResourceException {
        String path = parsePathFromUri( request.getResourceRef().toString() );
        if ( ! path.endsWith( "/" ) ) {
            response.redirectPermanent( path + "/" );
            return null;
        }

        String repositoryId = String.valueOf( request.getAttributes().get( REPOSITORY_ID_KEY ) );
        try {
            IndexingContext indexingContext =
                indexerManager.getRepositoryRemoteIndexContext( repositoryId );
            
            return createResponse( request, indexingContext );
        }
        catch ( NoSuchRepositoryException e ) {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e );
        }
    }
    
    protected Object createResponse( Request request, IndexingContext indexingContext )
            throws ResourceException {
        NexusIndexer indexer = indexerManager.getNexusIndexer();
        String path = parsePathFromUri( request.getResourceRef().getRemainingPart() );
        
        ContentListResourceResponse response = new ContentListResourceResponse();
        
        try {
            if ( "/".equals( path ) ) {
                // get root groups and finish
                Set<String> rootGroups = indexer.getRootGroups( indexingContext );
                for ( String group : rootGroups ) {
                   response.addData( createGroupResource( request, path, group ) );
                }
            }
            else {
                Set<String> allGroups = indexer.getAllGroups( indexingContext );

                ContentListResource rootResource = new ContentListResource();
                rootResource.setRelativePath( path );
                loadChildren( request, rootResource, indexingContext, allGroups );
                response.setData( rootResource.getChildren() );
            }
        }
        catch ( IOException e ) {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
        
        return response;
    }

    protected void loadChildren( Request request, ContentListResource rootResource, 
            IndexingContext indexingContext, Set<String> allGroups ) throws ResourceException {
        String path = rootResource.getRelativePath();
        Map<String,ContentListResource> folders = new HashMap<String,ContentListResource>(); 

        Set<ArtifactInfo> artifacts = getArtifacts( path, indexingContext );
        for ( ArtifactInfo ai : artifacts ) {
            NexusArtifact na = ai2Na( request, ai );
            if ( na == null ) {
                continue;
            }

            String versionKey = ai.artifactId + ":" + ai.version;
            ContentListResource versionResource = folders.get( versionKey );
            if ( versionResource == null ) {
                ContentListResource artifactResource = folders.get( ai.artifactId );
                if ( artifactResource == null ) {
                    artifactResource = createArtifactResource( request, path, ai );
                    rootResource.getChildren().add( artifactResource );
                    folders.put( ai.artifactId, artifactResource );
                }

                versionResource = createVersionResource( request, path, ai );
                artifactResource.getChildren().add( versionResource );
                folders.put( versionKey, versionResource );
            }
            
            versionResource.getChildren().add( createFileResource( request, path, ai, na.getResourceURI() ) );
        }

        Set<String> groups = getGroups( path, allGroups ); 
        for ( String group : groups ) {
            ContentListResource groupResource = findChild( rootResource, group );
            if ( groupResource == null ) {
                groupResource = createGroupResource( request, path, group );
                rootResource.getChildren().add( groupResource );
            }
            else {
                // if the folder has been created as an artifact name,
                // we need to check for possible nested groups as well,
                // otherwise ExtJS will consider the resource loaded,
                // and will never query it again
                loadChildren( request, groupResource, indexingContext, allGroups );
            }
        }
    }

    protected ContentListResource createGroupResource( Request request,
            String path, String group ) {
        ContentListResource groupResource = new ContentListResource();
        path += group + "/";
        groupResource.setText( group );
        groupResource.setLeaf( false );
        groupResource.setResourceURI( request.getResourceRef().getBaseRef() + path );
        groupResource.setRelativePath( path );
        groupResource.setLastModified( new Date() );
        groupResource.setSizeOnDisk( -1 );
        return groupResource;
    }

    protected ContentListResource createArtifactResource( Request request,
            String path, ArtifactInfo ai ) {
        ContentListResource artifactResource = new ContentListResource();
        path += ai.artifactId + "/";
        artifactResource.setText( ai.artifactId );
        artifactResource.setLeaf( false );
        artifactResource.setResourceURI( request.getResourceRef().getBaseRef() + path );
        artifactResource.setRelativePath( path );
        artifactResource.setLastModified( new Date( ai.lastModified ) );
        artifactResource.setSizeOnDisk( -1 );
        return artifactResource;
    }

    protected ContentListResource createVersionResource( Request request,
            String path, ArtifactInfo ai ) {
        path += ai.artifactId + "/" + ai.version + "/";
        ContentListResource versionResource = new ContentListResource();
        versionResource.setText( ai.version );
        versionResource.setLeaf( false );
        versionResource.setResourceURI( request.getResourceRef().getBaseRef() + path );
        versionResource.setRelativePath( path );
        versionResource.setLastModified( new Date( ai.lastModified ) );
        versionResource.setSizeOnDisk( -1 );
        return versionResource;
    }

    protected ContentListResource createFileResource( Request request,
            String path, ArtifactInfo ai, String resourceURI ) {
        String filename = ai.artifactId + "-" + ai.version + ".jar";
        path += ai.artifactId + "/" + ai.version + "/" + filename;
        ContentListResource fileResource = new ContentListResource();
        fileResource.setText( filename );
        fileResource.setLeaf( true );
        fileResource.setResourceURI( resourceURI );//request.getResourceRef().getBaseRef() + path );
        fileResource.setRelativePath( path );
        fileResource.setLastModified( new Date( ai.lastModified ) );
        fileResource.setSizeOnDisk( ai.size );
        return fileResource;
    }

    protected Set<String> getGroups( String path, Set<String> allGroups )
            throws ResourceException {
        path = path.substring( 1 ).replace( '/', '.' );
        int n = path.length();
        Set<String> result = new HashSet<String>();
        for ( String group : allGroups ) {
            if ( group.startsWith( path ) ) {
                group = group.substring( n );
                int nextDot = group.indexOf( '.' );
                if ( nextDot > -1 ) {
                    group = group.substring( 0, nextDot );
                }
                if ( ! result.contains( group ) ) {
                    result.add( group );
                }
            }
        }
        return result;
    }
    
    protected Set<ArtifactInfo> getArtifacts( String path, IndexingContext indexingContext )
            throws ResourceException {
        NexusIndexer indexer = indexerManager.getNexusIndexer();

        path = path.substring( 1 ).replace( '/', '.' );
        if ( path.endsWith( "." ) ) {
            path = path.substring( 0, path.length() - 1 );
        }

        try {
            Query q = new TermQuery( new Term( ArtifactInfo.GROUP_ID, path ) );
            FlatSearchRequest searchRequest = new FlatSearchRequest( q, indexingContext );
            FlatSearchResponse searchResponse = indexer.searchFlat( searchRequest );
            
            return searchResponse.getResults();
        } catch ( IOException e ) {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        } catch ( IndexContextInInconsistentStateException e ) {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
    }
    
    protected String parsePathFromUri( String parsedPath ) {

        // get rid of query part
        if (parsedPath.contains("?")) {
            parsedPath = parsedPath.substring(0, parsedPath.indexOf('?'));
        }

        // get rid of reference part
        if (parsedPath.contains("#")) {
            parsedPath = parsedPath.substring(0, parsedPath.indexOf('#'));
        }

        if (StringUtils.isEmpty(parsedPath)) {
            parsedPath = "/";
        }

        return parsedPath;
    }
    
    protected ContentListResource findChild( ContentListResource parent, String name ) {
        for ( Object child : parent.getChildren() ) {
            if ( child instanceof ContentListResource ) {
                ContentListResource resource = ( ContentListResource ) child;
                if ( name.equals( resource.getText() ) ) {
                    return resource;
                }
            } 
        }
        return null;
    }
}
