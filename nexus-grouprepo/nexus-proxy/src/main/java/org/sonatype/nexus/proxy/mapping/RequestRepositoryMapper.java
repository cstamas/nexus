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
package org.sonatype.nexus.proxy.mapping;

import java.util.List;

import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Interface RequestRepositoryMapper. These mappers are used in Routers, to narrow the number of searched
 * repositories using some technique.
 */
public interface RequestRepositoryMapper
    extends EventListener
{
    String ROLE = RequestRepositoryMapper.class.getName();

    /**
     * Gets the mapped repositories.
     * 
     * @param request the request
     * @param resolvedRepositories the resolved repositories, possibly a bigger set
     * @return the mapped repositories repoIds
     */
    List<Repository> getMappedRepositories( RepositoryRegistry registry, RepositoryItemUid uid,
        List<Repository> resolvedRepositories )
        throws NoSuchResourceStoreException;

}
