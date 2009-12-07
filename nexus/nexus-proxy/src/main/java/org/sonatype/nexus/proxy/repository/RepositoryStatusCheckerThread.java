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
package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

public class RepositoryStatusCheckerThread
    extends Thread
{
    private final ProxyRepository repository;

    public RepositoryStatusCheckerThread( ProxyRepository repository )
    {
        super();

        this.repository = repository;
    }

    public ProxyRepository getRepository()
    {
        return repository;
    }

    public void run()
    {
        try
        {
            while ( !isInterrupted() && getRepository().getProxyMode() != null )
            {
                if ( RepositoryStatusCheckMode.ALWAYS.equals( getRepository().getRepositoryStatusCheckMode() ) )
                {
                    if ( getRepository().getLocalStatus().shouldServiceRequest() )
                    {
                        getRepository()
                            .getRemoteStatus( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT ), false );
                    }
                }
                else if ( RepositoryStatusCheckMode.AUTO_BLOCKED_ONLY.equals( getRepository()
                    .getRepositoryStatusCheckMode() ) )
                {
                    if ( getRepository().getProxyMode().shouldAutoUnblock() )
                    {
                        getRepository()
                            .getRemoteStatus( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT ), false );
                    }
                }
                else if ( RepositoryStatusCheckMode.NEVER.equals( getRepository().getRepositoryStatusCheckMode() ) )
                {
                    // nothing
                }

                Thread.sleep( AbstractProxyRepository.REMOTE_STATUS_RETAIN_TIME );
            }
        }
        catch ( InterruptedException e )
        {
        }
    }
}
