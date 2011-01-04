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
package org.sonatype.nexus.proxy.registry;

import java.util.List;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Interface RepositoryRegistry. It holds the Repositories and repo groups.
 *
 * @author cstamas
 */
public interface RepositoryRegistry
{
    /**
     * Adds single repository.
     *
     * @param repository the repository
     */
    void addRepository( Repository repository );

    /**
     * Removes single repository.
     *
     * @param repoId the repo id
     * @throws NoSuchRepositoryException the no such repository exception
     */
    void removeRepository( String repoId )
        throws NoSuchRepositoryException;

    /**
     * Removes "silently" single repository: no events will be emitted.
     *
     * @param repoId the repo id
     * @throws NoSuchRepositoryException the no such repository exception
     */
    void removeRepositorySilently( String repoId )
        throws NoSuchRepositoryException;

    /**
     * Returns the list of Repositories that serves Proximity. The repo order within list follows repo rank, so
     * processing is possible by simply iterating over resulting list.
     *
     * @return a List<Repository>
     */
    List<Repository> getRepositories();

    /**
     * Returns the list of Repositories that serves Proximity and have facets as T available. The repo order within list
     * follows repo rank, so processing is possible by simply iterating over resulting list.
     *
     * @return a List<T>
     */
    <T> List<T> getRepositoriesWithFacet( Class<T> f );

    /**
     * Returns the requested Repository by ID.
     *
     * @param repoId the repo id
     * @return the repository
     * @throws NoSuchRepositoryException the no such repository exception
     */
    Repository getRepository( String repoId )
        throws NoSuchRepositoryException;

    /**
     * Returns the requested Repository by ID.
     *
     * @param repoId the repo id
     * @return the repository
     * @throws NoSuchRepositoryException the no such repository exception
     */
    <T> T getRepositoryWithFacet( String repoId, Class<T> f )
        throws NoSuchRepositoryException;

    /**
     * Checks for the existence of given repositoryId within this registry.
     *
     * @param repositoryId the repository id
     * @return boolean
     */
    boolean repositoryIdExists( String repositoryId );

    /**
     * Collect the groupIds where repository is member.
     *
     * @param repositoryId the repository id
     * @return list of groupId's where the repo appears as member
     */
    List<String> getGroupsOfRepository( String repositoryId );

    /**
     * Collect the groupIds where repository is member.
     *
     * @param repository the repository
     * @return list of group's where the repo appears as member
     */
    List<GroupRepository> getGroupsOfRepository( Repository repository);
}
