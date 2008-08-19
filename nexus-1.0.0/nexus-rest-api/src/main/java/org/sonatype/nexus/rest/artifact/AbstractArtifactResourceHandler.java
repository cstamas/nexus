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
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.ArtifactStoreRequest;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.ApplicationBridge;
import org.sonatype.nexus.rest.StorageFileItemRepresentation;

public class AbstractArtifactResourceHandler
    extends AbstractNexusResourceHandler
{
    public AbstractArtifactResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * Centralized way to create ResourceStoreRequests, since we have to fill in various things in Request context, like
     * authenticated username, etc.
     * 
     * @param isLocal
     * @return
     */
    protected ArtifactStoreRequest getResourceStoreRequest( boolean localOnly, String repositoryId,
        String repositoryGroupId, String g, String a, String v, String p, String c )
    {
        ArtifactStoreRequest result = new ArtifactStoreRequest(
            localOnly,
            repositoryId,
            repositoryGroupId,
            g,
            a,
            v,
            p,
            c );

        if ( getLogger().isLoggable( Level.FINE ) )
        {
            getLogger().log( Level.FINE, "Created ResourceStore request for " + result.getRequestPath() );
        }

        result
            .getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, getRequest().getClientInfo().getAddress() );

        if ( getRequest().getChallengeResponse() != null && getRequest().getChallengeResponse().getIdentifier() != null )
        {
            result.getRequestContext().put(
                AccessManager.REQUEST_USER,
                getRequest().getChallengeResponse().getIdentifier() );
        }

        if ( getRequest().isConfidential() )
        {
            result.getRequestContext().put( AccessManager.REQUEST_CONFIDENTIAL, Boolean.TRUE );

            // X509Certificate[] certs = (X509Certificate[]) context.getHttpServletRequest().getAttribute(
            // "javax.servlet.request.X509Certificate" );
            // if ( false ) // certs != null )
            // {
            // result.getRequestContext().put( CertificateBasedAccessDecisionVoter.REQUEST_CERTIFICATES, certs );
            // }
        }
        return result;
    }

    protected Representation getPom( Variant variant )
    {
        Form form = getRequest().getResourceRef().getQueryAsForm();

        // TODO: enable only one section retrieval of POM, ie. only mailing lists, or team members

        String groupId = form.getFirstValue( "g" );

        String artifactId = form.getFirstValue( "a" );

        String version = form.getFirstValue( "v" );

        String repositoryId = form.getFirstValue( "r" );

        if ( groupId == null || artifactId == null || version == null || repositoryId == null )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST );

            return null;
        }

        ArtifactStoreRequest gavRequest = getResourceStoreRequest(
            false,
            repositoryId,
            null,
            groupId,
            artifactId,
            version,
            null,
            null );

        try
        {
            Repository repository = getNexus().getRepository( repositoryId );

            if ( !MavenRepository.class.isAssignableFrom( repository.getClass() ) )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "This is not a Maven repository!" );

                return null;
            }

            InputStream pomContent = null;

            InputStreamReader ir = null;

            Model pom = null;

            try
            {
                StorageFileItem file = ( (MavenRepository) repository ).retrieveArtifactPom( gavRequest );

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

            return serialize( variant, pom );

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

    protected Representation getContent( Variant variant, boolean redirectTo )
    {
        Form form = getRequest().getResourceRef().getQueryAsForm();

        String groupId = form.getFirstValue( "g" );

        String artifactId = form.getFirstValue( "a" );

        String version = form.getFirstValue( "v" );

        String packaging = form.getFirstValue( "p" );

        String classifier = form.getFirstValue( "c" );

        String repositoryId = form.getFirstValue( "r" );

        if ( groupId == null || artifactId == null || version == null || repositoryId == null )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST );

            return null;
        }

        ArtifactStoreRequest gavRequest = getResourceStoreRequest(
            false,
            repositoryId,
            null,
            groupId,
            artifactId,
            version,
            packaging,
            classifier );

        try
        {
            Repository repository = getNexus().getRepository( repositoryId );

            if ( !MavenRepository.class.isAssignableFrom( repository.getClass() ) )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "This is not a Maven repository!" );

                return null;
            }

            StorageFileItem file = ( (MavenRepository) repository ).retrieveArtifact( gavRequest );

            if ( redirectTo )
            {
                String filePath = file.getRepositoryItemUid().getPath();

                if ( filePath.startsWith( RepositoryItemUid.PATH_SEPARATOR ) )
                {
                    filePath = filePath.substring( 1 );
                }
                
                filePath = "content/" + filePath;

                Reference repoRoot = calculateRepositoryReference( file.getRepositoryItemUid().getRepository().getId() );

                Reference fileReference = calculateReference( repoRoot, filePath );

                getResponse().setRedirectRef( fileReference );

                getResponse().setStatus( Status.REDIRECTION_PERMANENT );

                return null;
            }
            else
            {
                // TODO: this will not work without content disposition support in restlet!
                Representation result = new StorageFileItemRepresentation( file );

                return result;
            }

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
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Artifact not found." );
        }
        catch ( AccessDeniedException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_FORBIDDEN );
        }
        catch ( IOException e )
        {
            getLogger().log( Level.SEVERE, "IOException during retrieve of POM:", e );

            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );
        }

        return null;
    }

    protected void uploadArtifact( Representation representation )
    {
        if ( representation != null )
        {
            if ( MediaType.MULTIPART_FORM_DATA.equals( representation.getMediaType(), true ) )
            {
                // Just forcing creation of the temporary directory if it doesn't exist.
                // Because the upload is using the tmp directory outside of our code
                // Our method wont be called, which will create the directory on request
                getNexus().getNexusConfiguration().getTemporaryDirectory();

                FileItemFactory factory = (FileItemFactory) getContext().getAttributes().get(
                    ApplicationBridge.FILEITEM_FACTORY );

                RestletFileUpload upload = new RestletFileUpload( factory );

                List<FileItem> items;

                try
                {
                    items = upload.parseRequest( getRequest() );

                    // we have "nibbles": (params,fileA,[fileB])+
                    // the second file is optional
                    // if two files are present, one of them should be POM
                    String repositoryId = null;

                    boolean hasPom = false;

                    boolean isPom = false;

                    InputStream is = null;

                    String groupId = null;

                    String artifactId = null;

                    String version = null;

                    String classifier = null;

                    String packaging = null;

                    PomArtifactManager pomManager = new PomArtifactManager( getNexus()
                        .getNexusConfiguration().getTemporaryDirectory() );

                    for ( FileItem fi : items )
                    {
                        if ( fi.isFormField() )
                        {
                            // a parameter
                            if ( "r".equals( fi.getFieldName() ) )
                            {
                                repositoryId = fi.getString();
                            }
                            else if ( "g".equals( fi.getFieldName() ) )
                            {
                                groupId = fi.getString();
                            }
                            else if ( "a".equals( fi.getFieldName() ) )
                            {
                                artifactId = fi.getString();
                            }
                            else if ( "v".equals( fi.getFieldName() ) )
                            {
                                version = fi.getString();
                            }
                            else if ( "p".equals( fi.getFieldName() ) )
                            {
                                packaging = fi.getString();
                            }
                            else if ( "c".equals( fi.getFieldName() ) )
                            {
                                classifier = fi.getString();
                            }
                            else if ( "hasPom".equals( fi.getFieldName() ) )
                            {
                                hasPom = Boolean.parseBoolean( fi.getString() );
                            }
                        }
                        else
                        {
                            // a file
                            isPom = fi.getName().endsWith( ".pom" ) || fi.getName().endsWith( "pom.xml" );

                            ArtifactStoreRequest gavRequest;

                            if ( hasPom )
                            {
                                if ( isPom )
                                {
                                    pomManager.storeTempPomFile( fi.getInputStream() );

                                    is = pomManager.getTempPomFileInputStream();
                                }
                                else
                                {
                                    is = fi.getInputStream();
                                }

                                // this is ugly: since GAVRequest does not allow contructing
                                // without GAV, i am filling it with dummy values, and pomManager
                                // will set those to proper values
                                gavRequest = pomManager.getGAVRequestFromTempPomFile( getResourceStoreRequest(
                                    true,
                                    repositoryId,
                                    null,
                                    "G",
                                    "A",
                                    "V",
                                    "P",
                                    "C" ) );
                            }
                            else
                            {
                                is = fi.getInputStream();

                                gavRequest = getResourceStoreRequest(
                                    true,
                                    repositoryId,
                                    null,
                                    groupId,
                                    artifactId,
                                    version,
                                    packaging,
                                    classifier );
                            }

                            try
                            {
                                Repository repository = getNexus().getRepository( repositoryId );

                                if ( !MavenRepository.class.isAssignableFrom( repository.getClass() ) )
                                {
                                    getLogger().log( Level.SEVERE, "Upload to non maven repository attempted" );
                                    getResponse().setStatus(
                                        Status.CLIENT_ERROR_BAD_REQUEST,
                                        "This is not a Maven repository!" );

                                    return;
                                }
                                else
                                {
                                    // temporarily we disable SNAPSHOT upload
                                    // check is it a Snapshot repo
                                    MavenRepository mr = (MavenRepository) repository;

                                    if ( RepositoryPolicy.SNAPSHOT.equals( mr.getRepositoryPolicy() ) )
                                    {
                                        getLogger().log( Level.INFO, "Upload to SNAPSHOT maven repository attempted" );

                                        getResponse()
                                            .setStatus(
                                                Status.CLIENT_ERROR_BAD_REQUEST,
                                                "This is a Maven SNAPSHOT repository, and manual upload against it is forbidden!" );

                                        return;
                                    }
                                }

                                if ( !versionMatchesPolicy( gavRequest.getVersion(), ( (MavenRepository) repository )
                                    .getRepositoryPolicy() ) )
                                {
                                    getLogger().log(
                                        Level.SEVERE,
                                        "Version (" + gavRequest.getVersion() + ") and Repository Policy mismatch" );
                                    getResponse().setStatus(
                                        Status.CLIENT_ERROR_BAD_REQUEST,
                                        "The version " + gavRequest.getVersion()
                                            + " does not match the repository policy!" );

                                    return;
                                }

                                if ( isPom )
                                {
                                    ( (MavenRepository) repository ).storeArtifactPom( gavRequest, is, null );

                                    isPom = false;
                                }
                                else
                                {
                                    if ( hasPom )
                                    {
                                        ( (MavenRepository) repository ).storeArtifact( gavRequest, is, null );
                                    }
                                    else
                                    {
                                        ( (MavenRepository) repository ).storeArtifactWithGeneratedPom(
                                            gavRequest,
                                            is,
                                            null );
                                    }
                                }
                            }
                            catch ( IllegalArgumentException e )
                            {
                                getLogger().log( Level.INFO, "Cannot upload!", e );

                                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage() );

                                return;
                            }
                        }
                    }

                    if ( hasPom )
                    {
                        pomManager.removeTempPomFile();
                    }

                    getResponse().setStatus( Status.SUCCESS_CREATED );

                }
                catch ( StorageException e )
                {
                    getLogger().log( Level.SEVERE, "StorageException during retrieve:", e );

                    getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );
                }
                catch ( NoSuchResourceStoreException e )
                {
                    getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "No repository with id=" + "" );
                }
                catch ( RepositoryNotAvailableException e )
                {
                    getResponse().setStatus( Status.SERVER_ERROR_SERVICE_UNAVAILABLE );
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
                catch ( Exception e )
                {
                    getLogger().log( Level.SEVERE, "Exception during upload:", e );

                    getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST );
                }
            }
        }
        else
        {
            // POST request with no entity.
            getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST );
        }

    }

    private boolean versionMatchesPolicy( String version, RepositoryPolicy policy )
    {
        boolean result = false;

        if ( ( RepositoryPolicy.SNAPSHOT.equals( policy ) && VersionUtils.isSnapshot( version ) )
            || ( RepositoryPolicy.RELEASE.equals( policy ) && !VersionUtils.isSnapshot( version ) ) )
        {
            result = true;
        }

        return result;
    }
}
