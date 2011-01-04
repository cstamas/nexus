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

import java.util.Map;
import java.util.Set;

/**
 * This is the registry of known repository types. Just like RepositoryRegistry holds the "active" instances of
 * Repositories, this registry does the discovery of them. Hint: we are using String for role intentionally, to be able
 * to do reloads of plugins that contributes new repository roles to system.
 * 
 * @author cstamas
 */
public interface RepositoryTypeRegistry
{
    /**
     * Returns the unmodifiable set of repo type descriptors that are known that provides Repository components.
     * 
     * @return a modifiable set of repository type descriptors or empty set.
     */
    Set<RepositoryTypeDescriptor> getRegisteredRepositoryTypeDescriptors();

    /**
     * Registers a repo type.
     * 
     * @return a modifiable set of repository type descriptors or empty set.
     */
    boolean registerRepositoryTypeDescriptors( RepositoryTypeDescriptor d );

    /**
     * Deregisters a repo type.
     * 
     * @return a modifiable set of repository type descriptors or empty set.
     */
    boolean unregisterRepositoryTypeDescriptors( RepositoryTypeDescriptor d );

    /**
     * Returns an unmodifiable set of FQN of classes that are known that provides Repository components.
     * 
     * @return a set of repository type descriptors or empty set.
     */
    Set<String> getRepositoryRoles();

    /**
     * Returns the set of hints for the given repository role.
     * 
     * @param role
     * @return a set of repository hints or empty set.
     */
    Set<String> getExistingRepositoryHints( String role );

    /**
     * Returns the type descriptor for the given role+hint combination.
     * 
     * @param role
     * @param hint
     * @return the type descriptor or null if there is none for this combination of role and hint.
     */
    RepositoryTypeDescriptor getRepositoryTypeDescriptor( String role, String hint );

    /**
     * Returns the available content classes as unmodifiable map.
     * 
     * @return
     */
    Map<String, ContentClass> getContentClasses();
    
    /**
     * Returns a list of content class ids that are compatible with teh supplied content class
     * 
     * @param contentClass
     * @return
     */
    Set<String> getCompatibleContentClasses( ContentClass contentClass );

    /**
     * Returns the ContentClass for the given Repository component.
     * 
     * @param role
     * @param hint
     * @return the content class instance or null if repository does not exists.
     */
    ContentClass getRepositoryContentClass( String role, String hint );
}
