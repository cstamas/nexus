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
package org.sonatype.nexus.proxy.router;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Request route holds the information how will be an incoming request processed.
 * 
 * @author cstamas
 */
public class RequestRoute
{
    private Repository targetedRepository;

    private String repositoryPath;

    private String strippedPrefix;

    private String originalRequestPath;

    private int requestDepth;

    private ResourceStoreRequest resourceStoreRequest;

    public boolean isRepositoryHit()
    {
        return targetedRepository != null;
    }

    public Repository getTargetedRepository()
    {
        return targetedRepository;
    }

    public void setTargetedRepository( Repository targetedRepository )
    {
        this.targetedRepository = targetedRepository;
    }

    public String getRepositoryPath()
    {
        return repositoryPath;
    }

    public void setRepositoryPath( String repositoryPath )
    {
        this.repositoryPath = repositoryPath;
    }

    public String getStrippedPrefix()
    {
        return strippedPrefix;
    }

    public void setStrippedPrefix( String strippedPrefix )
    {
        this.strippedPrefix = strippedPrefix;
    }

    public String getOriginalRequestPath()
    {
        return originalRequestPath;
    }

    public void setOriginalRequestPath( String originalRequestPath )
    {
        this.originalRequestPath = originalRequestPath;
    }

    public int getRequestDepth()
    {
        return requestDepth;
    }

    public void setRequestDepth( int depth )
    {
        this.requestDepth = depth;
    }

    public ResourceStoreRequest getResourceStoreRequest()
    {
        return resourceStoreRequest;
    }

    public void setResourceStoreRequest( ResourceStoreRequest resourceStoreRequest )
    {
        this.resourceStoreRequest = resourceStoreRequest;
    }
}
