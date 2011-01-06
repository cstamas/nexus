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
package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.walker.AbstractFileWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

public class DeletionNotifierWalker
    extends AbstractFileWalkerProcessor
{
    private final ApplicationEventMulticaster applicationEventMulticaster;

    private final ResourceStoreRequest request;

    public DeletionNotifierWalker( ApplicationEventMulticaster applicationEventMulticaster, ResourceStoreRequest request )
    {
        this.applicationEventMulticaster = applicationEventMulticaster;

        this.request = request;
    }

    @Override
    protected void processFileItem( WalkerContext ctx, StorageFileItem item )
    {
        item.getItemContext().putAll( request.getRequestContext() );
        
        if( request.getRequestContext().containsKey( AccessManager.REQUEST_USER ) )
        {
            item.getAttributes().put( AccessManager.REQUEST_USER, request.getRequestContext().get( AccessManager.REQUEST_USER ) +"" );
        }
        
        if( request.getRequestContext().containsKey( AccessManager.REQUEST_REMOTE_ADDRESS ) )
        {
            item.getAttributes().put( AccessManager.REQUEST_REMOTE_ADDRESS, request.getRequestContext().get( AccessManager.REQUEST_REMOTE_ADDRESS ) +"" );
        }
        
        // just fire it, and someone will eventually catch it
        applicationEventMulticaster.notifyEventListeners( new RepositoryItemEventDelete(
            ctx.getRepository(),
            item ) );
    }

}
