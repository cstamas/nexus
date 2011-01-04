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
package org.sonatype.nexus.rest.repositories;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.cache.CacheStatistics;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.NoSuchRepositoryAccessException;
import org.sonatype.nexus.rest.model.RepositoryMetaResource;
import org.sonatype.nexus.rest.model.RepositoryMetaResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RepositoryMetaPlexusResource" )
@Path( RepositoryMetaPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class RepositoryMetaPlexusResource
    extends AbstractRepositoryPlexusResource
{
    public static final String RESOURCE_URI = "/repositories/{" + REPOSITORY_ID_KEY + "}/meta"; 

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repositories/*/meta", "authcBasic,perms[nexus:repometa]" );
    }

    /**
     * Retrieve metadata of an existing repository.
     * 
     * @param repositoryId The repository to retrieve the metadata for.
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY ) }, 
                              output = RepositoryMetaResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String repoId = this.getRepositoryId( request );
        try
        {
            Repository repository = getRepositoryRegistry().getRepository( repoId );

            RepositoryMetaResource resource = new RepositoryMetaResource();

            resource.setId( repoId );

            resource.setRepoType( getRestRepoType( repository ) );

            resource.setFormat( repository.getRepositoryContentClass().getId() );
            
            for ( GroupRepository group : getRepositoryRegistry().getGroupsOfRepository( repository ) )
            {
                resource.addGroup( group.getId() );
            }

            /*
            NEXUS-2790 removing as calculation takes too long in certain circumstances
            will eventually be reimplemented
            
            File localPath = org.sonatype.nexus.util.FileUtils.getFileFromUrl( repository.getLocalUrl() );
            
            try
            {
                resource.setSizeOnDisk( FileUtils.sizeOfDirectory( localPath ) );

                resource.setFileCountInRepository( org.sonatype.nexus.util.FileUtils.filesInDirectory( localPath ) );
            }
            catch ( IllegalArgumentException e )
            {
                // the repo is maybe virgin, so the dir is not created until some request needs it
            }
            */

            // mustang is able to get this with File.getUsableFreeSpace();
            resource.setFreeSpaceOnDisk( -1 );

            CacheStatistics stats = repository.getNotFoundCache().getStatistics();

            resource.setNotFoundCacheSize( stats.getSize() );

            resource.setNotFoundCacheHits( stats.getHits() );

            resource.setNotFoundCacheMisses( stats.getMisses() );

            resource.setLocalStorageErrorsCount( 0 );

            resource.setRemoteStorageErrorsCount( 0 );

            RepositoryMetaResourceResponse result = new RepositoryMetaResourceResponse();

            result.setData( resource );

            return result;
        }
        catch ( NoSuchRepositoryAccessException e )
        {
            getLogger().warn( "Repository access denied, id=" + repoId );

            throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN, "Access Denied to Repository" );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Repository not found, id=" + repoId );

            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found" );
        }
    }

}
