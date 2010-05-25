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
package org.sonatype.nexus.test.utils;

import java.util.Comparator;

import org.sonatype.security.model.CUser;


public class UserComparator implements Comparator<CUser>
{

    public int compare( CUser user1, CUser user2 )
    {
        // quick outs
        if( user1 == null || user2 == null)
        {
            return -1;
        }
        
        if( user1 == user2 || user1.equals( user2 ))
        {
            return 0;
        }
        
        if ( user1.getEmail() == null )
        {
            if ( user2.getEmail() != null )
                return -1;
        }
        else if ( !user1.getEmail().equals( user2.getEmail() ) )
            return -1;
        /*if ( user1.getModelEncoding() == null )
        {
            if ( user2.getModelEncoding() != null )
                return -1;
        }
        else if ( !user1.getModelEncoding().equals( user2.getModelEncoding() ) )
            return -1;*/
        if ( user1.getFirstName() == null )
        {
            if ( user2.getFirstName() != null )
                return -1;
        }
        else if ( !user1.getLastName().equals( user2.getLastName() ) )
            return -1;
        
        if ( user1.getLastName() == null )
        {
            if ( user2.getLastName() != null )
                return -1;
        }
        else if ( !user1.getLastName().equals( user2.getLastName() ) )
            return -1;
//        if ( user1.getPassword() == null )
//        {
//            if ( user2.getPassword() != null )
//                return -1;
//        }
//        else if ( !user1.getPassword().equals( user2.getPassword() ) )
//            return -1;
//        if ( user1.getRoles() == null )
//        {
//            if ( user2.getRoles() != null )
//                return -1;
//        }
//        else if ( !user1.getRoles().equals( user2.getRoles() ) )
//            return -1;
        if ( user1.getStatus() == null )
        {
            if ( user2.getStatus() != null )
                return -1;
        }
        else if ( !user1.getStatus().equals( user2.getStatus() ) )
            return -1;
        if ( user1.getId() == null )
        {
            if ( user2.getId() != null )
                return -1;
        }
        else if ( !user1.getId().equals( user2.getId() ) )
            return -1;
        return 0;
    }
}
