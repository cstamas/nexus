/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy;

/**
 * IllegalRequestException is thrown when an illegal request is tried against a ResourceStore.
 * 
 * @author cstamas
 */
public class IllegalRequestException
    extends IllegalOperationException
{
    private static final long serialVersionUID = -1683012685732920168L;

    private final ResourceStoreRequest request;

    public IllegalRequestException( ResourceStoreRequest request, String message )
    {
        super( message );

        this.request = request;
    }

    public ResourceStoreRequest getRequest()
    {
        return request;
    }

}
