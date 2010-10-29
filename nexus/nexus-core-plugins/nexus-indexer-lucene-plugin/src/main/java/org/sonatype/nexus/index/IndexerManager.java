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
package org.sonatype.nexus.index;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.treeview.TreeNode;
import org.sonatype.nexus.index.treeview.TreeNodeFactory;
import org.sonatype.nexus.index.treeview.TreeViewRequest;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

public interface IndexerManager
{
    // ----------------------------------------------------------------------------
    // Context management et al
    // ----------------------------------------------------------------------------

    void shutdown( boolean deleteFiles )
        throws IOException;

    void resetConfiguration();

    void addRepositoryIndexContext( String repositoryId )
        throws IOException, NoSuchRepositoryException;

    void removeRepositoryIndexContext( String repositoryId, boolean deleteFiles )
        throws IOException, NoSuchRepositoryException;

    void updateRepositoryIndexContext( String repositoryId )
        throws IOException, NoSuchRepositoryException;

    /**
     * Returns the local index (the true index for hosted ones, and the true cacheds index for proxy reposes). Every
     * repo has local index.
     * 
     * @param repositoryId
     * @return
     * @throws NoSuchRepositoryException
     */
    IndexingContext getRepositoryLocalIndexContext( String repositoryId )
        throws NoSuchRepositoryException;

    /**
     * Returns the remote index. Only proxy repositories have remote index, otherwise null is returnded.
     * 
     * @param repositoryId
     * @return
     * @throws NoSuchRepositoryException
     */
    IndexingContext getRepositoryRemoteIndexContext( String repositoryId )
        throws NoSuchRepositoryException;

    /**
     * Returns the "best" indexing context. If it has remoteIndex, and it is bigger then local, remote is considered
     * "best", otherwise local.
     * 
     * @param repositoryId
     * @return
     * @throws NoSuchRepositoryException
     */
    IndexingContext getRepositoryBestIndexContext( String repositoryId )
        throws NoSuchRepositoryException;

    /**
     * Flags an indexing context should be searched in global searches or not.
     * 
     * @param repositoryId
     * @param searchable
     * @throws IOException
     * @throws NoSuchRepositoryException
     */
    void setRepositoryIndexContextSearchable( String repositoryId, boolean searchable )
        throws IOException, NoSuchRepositoryException;

    // ----------------------------------------------------------------------------
    // Publish the used NexusIndexer
    // ----------------------------------------------------------------------------

    @Deprecated
    NexusIndexer getNexusIndexer();

    // ----------------------------------------------------------------------------
    // adding/removing on the fly
    // ----------------------------------------------------------------------------

    void addItemToIndex( Repository repository, StorageItem item )
        throws IOException;

    void removeItemFromIndex( Repository repository, StorageItem item )
        throws IOException;

    // ----------------------------------------------------------------------------
    // Reindexing related (will do local-scan, remote-download, merge, publish)
    // ----------------------------------------------------------------------------

    void reindexAllRepositories( String path, boolean fullReindex )
        throws IOException;

    void reindexRepository( String path, String repositoryId, boolean fullReindex )
        throws NoSuchRepositoryException, IOException;

    void reindexRepositoryGroup( String path, String repositoryGroupId, boolean fullReindex )
        throws NoSuchRepositoryException, IOException;

    void resetGroupIndex( String groupId, boolean purgeOnly )
        throws NoSuchRepositoryException, IOException;

    // ----------------------------------------------------------------------------
    // Downloading remote indexes (will do remote-download, merge only)
    // ----------------------------------------------------------------------------

    void downloadAllIndex()
        throws IOException;

    void downloadRepositoryIndex( String repositoryId )
        throws IOException, NoSuchRepositoryException;

    void downloadRepositoryGroupIndex( String repositoryGroupId )
        throws IOException, NoSuchRepositoryException;

    // ----------------------------------------------------------------------------
    // Publishing index (will do publish only)
    // ----------------------------------------------------------------------------

    void publishAllIndex()
        throws IOException;

    void publishRepositoryIndex( String repositoryId )
        throws IOException, NoSuchRepositoryException;

    void publishRepositoryGroupIndex( String repositoryGroupId )
        throws IOException, NoSuchRepositoryException;

    // ----------------------------------------------------------------------------
    // Identify
    // ----------------------------------------------------------------------------

    @Deprecated
    ArtifactInfo identifyArtifact( String type, String checksum )
        throws IOException;

    ArtifactInfo identifyArtifact( Field field, String data )
        throws IOException;

    // ----------------------------------------------------------------------------
    // Combined searching
    // ----------------------------------------------------------------------------

    @Deprecated
    FlatSearchResponse searchArtifactFlat( String term, String repositoryId, Integer from, Integer count,
                                           Integer hitLimit )
        throws NoSuchRepositoryException;

    @Deprecated
    FlatSearchResponse searchArtifactClassFlat( String term, String repositoryId, Integer from, Integer count,
                                                Integer hitLimit )
        throws NoSuchRepositoryException;

    @Deprecated
    FlatSearchResponse searchArtifactFlat( String gTerm, String aTerm, String vTerm, String pTerm, String cTerm,
                                           String repositoryId, Integer from, Integer count, Integer hitLimit )
        throws NoSuchRepositoryException;

    IteratorSearchResponse searchArtifactIterator( String term, String repositoryId, Integer from, Integer count,
                                                   Integer hitLimit, boolean uniqueRGA, SearchType searchType,
                                                   List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException;

    IteratorSearchResponse searchArtifactClassIterator( String term, String repositoryId, Integer from, Integer count,
                                                        Integer hitLimit, SearchType searchType,
                                                        List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException;

    IteratorSearchResponse searchArtifactIterator( String gTerm, String aTerm, String vTerm, String pTerm,
                                                   String cTerm, String repositoryId, Integer from, Integer count,
                                                   Integer hitLimit, boolean uniqueRGA, SearchType searchType,
                                                   List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException;

    IteratorSearchResponse searchArtifactSha1ChecksumIterator( String sha1Checksum, String repositoryId, Integer from,
                                                               Integer count, Integer hitLimit,
                                                               List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException;

    // ----------------------------------------------------------------------------
    // Query construction
    // ----------------------------------------------------------------------------

    Query constructQuery( String field, String query );

    // ----------------------------------------------------------------------------
    // Tree nodes
    // ----------------------------------------------------------------------------

    /**
     * @deprecated Use {@link #listNodes(TreeViewRequest) instead.
     */
    TreeNode listNodes( TreeNodeFactory factory, Repository repository, String path );

    TreeNode listNodes( TreeViewRequest request );

    void optimizeRepositoryIndex( String repositoryId )
        throws IOException;;

    void optimizeGroupIndex( String groupId )
        throws IOException;

    void optimizeAllRepositoriesIndex()
        throws IOException;;

}
