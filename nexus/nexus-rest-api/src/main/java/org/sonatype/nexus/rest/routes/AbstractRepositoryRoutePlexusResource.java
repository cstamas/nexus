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
package org.sonatype.nexus.rest.routes;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.mapping.RequestRepositoryMapper;
import org.sonatype.nexus.proxy.mapping.RepositoryPathMapping.MappingType;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;

/**
 * Abstract base class for route resource handlers.
 * 
 * @author cstamas
 */
public abstract class AbstractRepositoryRoutePlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String ROUTE_ID_KEY = "routeId";

    @Requirement
    private RequestRepositoryMapper repositoryMapper;

    protected RequestRepositoryMapper getRepositoryMapper()
    {
        return repositoryMapper;
    }

    /**
     * Creating a list of member reposes. Since this method is used in two Resource subclasses too, and those are
     * probably mapped to different bases, a listBase param is needed to generate a correct URI, from the actual
     * subclass effective mapping.
     * 
     * @param listBase
     * @param reposList
     * @param request
     * @return
     * @throws NoSuchRepositoryException
     * @throws ResourceException
     */
    protected List<RepositoryRouteMemberRepository> getRepositoryRouteMemberRepositoryList( Reference listBase,
                                                                                            List<String> reposList,
                                                                                            Request request )
        throws NoSuchRepositoryException, ResourceException
    {
        List<RepositoryRouteMemberRepository> members =
            new ArrayList<RepositoryRouteMemberRepository>( reposList.size() );

        for ( String repoId : reposList )
        {
            RepositoryRouteMemberRepository member = new RepositoryRouteMemberRepository();

            if ( "*".equals( repoId ) )
            {
                member.setId( "*" );

                member.setName( "ALL" );

                member.setResourceURI( null );
            }
            else
            {
                member.setId( repoId );

                member.setName( getRepositoryRegistry().getRepository( repoId ).getName() );

                member.setResourceURI( createChildReference( request, this, repoId ).toString() );
            }

            members.add( member );
        }

        return members;
    }

    protected MappingType resource2configType( String type )
    {
        if ( RepositoryRouteResource.INCLUSION_RULE_TYPE.equals( type ) )
        {
            return MappingType.INCLUSION;
        }
        else if ( RepositoryRouteResource.EXCLUSION_RULE_TYPE.equals( type ) )
        {
            return MappingType.EXCLUSION;
        }
        else if ( RepositoryRouteResource.BLOCKING_RULE_TYPE.equals( type ) )
        {
            return MappingType.BLOCKING;
        }
        else
        {
            return null;
        }
    }

    protected String config2resourceType( MappingType type )
    {
        if ( MappingType.INCLUSION.equals( type ) )
        {
            return RepositoryRouteResource.INCLUSION_RULE_TYPE;
        }
        else if ( MappingType.EXCLUSION.equals( type ) )
        {
            return RepositoryRouteResource.EXCLUSION_RULE_TYPE;
        }
        else if ( MappingType.BLOCKING.equals( type ) )
        {
            return RepositoryRouteResource.BLOCKING_RULE_TYPE;
        }
        else
        {
            return null;
        }
    }

}
