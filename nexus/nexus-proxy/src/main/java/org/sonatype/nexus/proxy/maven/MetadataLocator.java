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
package org.sonatype.nexus.proxy.maven;

import java.io.IOException;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.IllegalArtifactCoordinateException;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.Plugin;
import org.apache.maven.model.Model;

/**
 * An adapter in charge for doing the IO against the storage, hiding the fact where it runs.
 * 
 * @author cstamas
 */
public interface MetadataLocator
{
    /**
     * Calculates the GAV for the request.
     * 
     * @param request
     * @return
     */
    Gav getGavForRequest( ArtifactStoreRequest request )
        throws IllegalArtifactCoordinateException;

    /**
     * Constructs a Plugin elem for given request. It returns null if the artifacts's POM pointed out by request is not
     * "maven-plugin".
     * 
     * @param request
     * @return
     * @throws IOException
     */
    Plugin extractPluginElementFromPom( ArtifactStoreRequest request )
        throws IOException;

    /**
     * Constructs a POM for given request.
     * 
     * @param request
     * @return
     * @throws IOException
     */
    Model retrievePom( ArtifactStoreRequest request )
        throws IOException;

    /**
     * Gets the packaging from POM for given request.
     * 
     * @param request
     * @return packaging, or null if not found on the request path
     * @throws IOException
     */
    String retrievePackagingFromPom( ArtifactStoreRequest request )
        throws IOException;

    /**
     * Retrieves the GAV level metadata.
     * 
     * @param request
     * @return
     * @throws IOException
     */
    Metadata retrieveGAVMetadata( ArtifactStoreRequest request )
        throws IOException;

    /**
     * Retrieves the GA level metadata.
     * 
     * @param request
     * @return
     * @throws IOException
     */
    Metadata retrieveGAMetadata( ArtifactStoreRequest request )
        throws IOException;

    /**
     * Retrieves the G level metadata.
     * 
     * @param request
     * @return
     * @throws IOException
     */
    Metadata retrieveGMetadata( ArtifactStoreRequest request )
        throws IOException;

    /**
     * Stores the GAV level metadata.
     * 
     * @param request
     * @return
     * @throws IOException
     */
    void storeGAVMetadata( ArtifactStoreRequest request, Metadata metadata )
        throws IOException;

    /**
     * Stores the GA level metadata.
     * 
     * @param request
     * @return
     * @throws IOException
     */
    void storeGAMetadata( ArtifactStoreRequest request, Metadata metadata )
        throws IOException;

    /**
     * Stores the G level metadata.
     * 
     * @param request
     * @return
     * @throws IOException
     */
    void storeGMetadata( ArtifactStoreRequest request, Metadata metadata )
        throws IOException;
}
