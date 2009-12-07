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
package org.sonatype.nexus.proxy.item;

import java.io.IOException;
import java.io.InputStream;

/**
 * The Interface ContentLocator. Implements a strategy to fetch content of an item.
 * 
 * @author cstamas
 */
public interface ContentLocator
{
    /**
     * Gets the content. It has to be closed by the caller explicitly.
     * 
     * @return the content
     * @throws IOException Signals that an I/O exception has occurred.
     */
    InputStream getContent()
        throws IOException;

    /**
     * Returns the MIME type of the content.
     * 
     * @return
     */
    String getMimeType();

    /**
     * Checks if is reusable.
     * 
     * @return true, if is reusable
     */
    boolean isReusable();
}
