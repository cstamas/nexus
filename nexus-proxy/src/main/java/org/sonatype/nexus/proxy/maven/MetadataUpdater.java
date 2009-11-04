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
import java.util.Collection;

import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;

/**
 * A metadata updater that offers simple metadata management services.
 * 
 * @author cstamas
 */
public interface MetadataUpdater
{
    //
    // "Single shot" methods, used from Nexus to maintain metadata on-the-fly
    //

    /**
     * Calling this method updates the GAV, GA and G metadatas accordingly. It senses whether it is a snapshot or not.
     * 
     * @param req
     */
    void deployArtifact( ArtifactStoreRequest request )
        throws IOException, IllegalArtifactCoordinateException;

    /**
     * Calling this method updates the GAV, GA and G metadatas accordingly. It senses whether it is a snapshot or not.
     * 
     * @param req
     */
    void undeployArtifact( ArtifactStoreRequest request )
        throws IOException, IllegalArtifactCoordinateException;

    //
    // "Multi shot" methods, used from Nexus/CLI tools to maintain metadata in batch/scanning mode
    //

    /**
     * Calling this method <b>replaces</b> the GAV, GA and G metadatas accordingly.
     * 
     * @param req
     */
    void deployArtifacts( Collection<ArtifactStoreRequest> requests )
        throws IOException;

    /**
     * Calling this method <b>replaces</b> the GAV, GA and G metadatas accordingly.
     * 
     * @param req
     */
    void undeployArtifacts( Collection<ArtifactStoreRequest> requests )
        throws IOException;

    /**
     * Give me a coll, and i will createate the metadata.
     * 
     * @param coll
     * @param locator
     * @throws IOException
     */
    void recreateMetadata( StorageCollectionItem coll )
        throws IOException;
}
