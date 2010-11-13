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

import java.util.List;
import java.util.Map;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

/**
 * A searcher is able to perform artifact info searches based on key/value search terms. Note that this is an
 * intermediate step towards future Nexus pluggable indexing and should not be considered public api.
 * 
 * @author Alin Dreghiciu
 */
public interface Searcher
{

    /**
     * Answers the question: can this searcher be used to search for the available terms?
     * 
     * @param terms available terms
     * @return true if searcher can be used to search for the available terms, false oterwise
     */
    boolean canHandle( Map<String, String> terms );

    /**
     * Returns the default "search type", that this Searcher wants. Naturally, this is overridable, see
     * flatIteratorSearch() method.
     * 
     * @return
     */
    SearchType getDefaultSearchType();

    /**
     * Searches for artifacts based on available terms.
     * 
     * @param terms search terms
     * @param repositoryId repository id of the repository to be searched ir null if the search should be performed on
     *            all repositories that suports indexing
     * @param from offset of first search result
     * @param count number of search results to be retrieved
     * @return search results
     * @throws NoSuchRepositoryException - If there is no repository with specified repository id
     * @deprecated use flatIteratorSearch() instead.
     */
    FlatSearchResponse flatSearch( Map<String, String> terms, String repositoryId, Integer from, Integer count,
                                   Integer hitLimit )
        throws NoSuchRepositoryException;

    /**
     * Searches for artifacts based on available terms.
     * 
     * @param terms search terms
     * @param repositoryId repository id of the repository to be searched ir null if the search should be performed on
     *            all repositories that suports indexing
     * @param from offset of first search result
     * @param count number of search results to be retrieved
     * @return search results
     * @throws NoSuchRepositoryException - If there is no repository with specified repository id
     */
    IteratorSearchResponse flatIteratorSearch( Map<String, String> terms, String repositoryId, Integer from,
                                               Integer count, Integer hitLimit, boolean uniqueRGA, SearchType searchType, 
                                               List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException;
}
