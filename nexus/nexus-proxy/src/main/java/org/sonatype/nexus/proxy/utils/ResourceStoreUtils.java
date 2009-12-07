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
package org.sonatype.nexus.proxy.utils;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Simple utils regarding stores.
 * 
 * @author cstamas
 */
public class ResourceStoreUtils
{

    /**
     * A simple utility to "format" a list of stores for logging or other output.
     * 
     * @param stores
     * @return
     */
    public static String getResourceStoreListAsString( List<? extends ResourceStore> stores )
    {
        if ( stores == null )
        {
            return "[]";
        }

        ArrayList<String> repoIdList = new ArrayList<String>( stores.size() );

        for ( ResourceStore store : stores )
        {
            if ( store instanceof Repository )
            {
                repoIdList.add( ( (Repository) store ).getId() );
            }
            else
            {
                repoIdList.add( store.getClass().getName() );
            }
        }

        return StringUtils.join( repoIdList.iterator(), ", " );
    }

}
