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
package org.sonatype.nexus.proxy.access;

import java.util.Collections;
import java.util.List;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A simple AccessManager implementation that allows everybody to access the Proximity core.
 * 
 * @author t.cservenak
 * @plexus.component instantiation-strategy="per-lookup" role-hint="open"
 */
public class OpenAccessManager
    implements AccessManager
{

    public void decide( ResourceStoreRequest request, Repository repository, RepositoryPermission permission )
        throws AccessDeniedException
    {
        // this access manager is open, everybody has access to everything since
        // it never throws AccessDeniedEx
    }

    public List<AccessDecisionVoter> getVoters()
    {
        return Collections.emptyList();
    }

}
