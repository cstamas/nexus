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
package org.sonatype.sample.wrapper;

import java.io.File;
import java.io.IOException;

/**
 * Simple interface for persisted configuration files.
 * 
 * @author cstamas
 */
public interface PersistedConfiguration
{
    /**
     * Resets the configuration by reloading original wrapper.conf it is pointed to. Looses all changes made so fat not
     * saved.
     * 
     * @throws IOException
     */
    void reset()
        throws IOException;

    /**
     * Persists back the loaded wrapper.conf. reset() will not revert to original!
     * 
     * @throws IOException
     */
    void save()
        throws IOException;

    /**
     * Persists the wrapper.conf to the supplied file, not overwriting the original. reset() will revert to original!
     * 
     * @throws IOException
     */
    void save( File target )
        throws IOException;
}
