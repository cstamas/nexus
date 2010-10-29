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
package org.sonatype.nexus.rest.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.apache.lucene.store.AlreadyClosedException;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.index.ArtifactInfoFilter;
import org.sonatype.nexus.index.IteratorSearchResponse;
import org.sonatype.nexus.index.KeywordSearcher;
import org.sonatype.nexus.index.MAVEN;
import org.sonatype.nexus.index.MavenCoordinatesSearcher;
import org.sonatype.nexus.index.SearchType;
import org.sonatype.nexus.index.Searcher;
import org.sonatype.nexus.index.UniqueArtifactFilterPostprocessor;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.AbstractIndexerNexusPlexusResource;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.tasks.ReindexTask;

public abstract class AbstractIndexPlexusResource
    extends AbstractIndexerNexusPlexusResource
{
    private static final int HIT_LIMIT = 500;

    private static final int COLLAPSE_OVERRIDE_TRESHOLD = 35;

    public static final String DOMAIN = "domain";

    public static final String DOMAIN_REPOSITORIES = "repositories";

    public static final String DOMAIN_REPO_GROUPS = "repo_groups";

    public static final String TARGET_ID = "target";

    @Requirement
    private NexusScheduler nexusScheduler;

    @Requirement( role = Searcher.class )
    private List<Searcher> m_searchers;

    public AbstractIndexPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();

        final Map<String, String> terms = new HashMap<String, String>();
        for ( Parameter parameter : form )
        {
            terms.put( parameter.getName(), parameter.getValue() );
        }

        Integer from = null;
        Integer count = null;
        Boolean exact = null;
        Boolean expandVersion = null;
        Boolean expandPackaging = null;
        Boolean expandClassifier = null;
        Boolean collapseResults = Boolean.FALSE;

        if ( form.getFirstValue( "from" ) != null )
        {
            try
            {
                from = Integer.valueOf( form.getFirstValue( "from" ) );
            }
            catch ( NumberFormatException e )
            {
                from = null;
            }
        }

        if ( form.getFirstValue( "count" ) != null )
        {
            try
            {
                count = Integer.valueOf( form.getFirstValue( "count" ) );
            }
            catch ( NumberFormatException e )
            {
                count = null;
            }
        }

        if ( form.getFirstValue( "exact" ) != null )
        {
            exact = Boolean.valueOf( form.getFirstValue( "exact" ) );
        }

        if ( form.getFirstValue( "versionexpand" ) != null )
        {
            expandVersion = Boolean.valueOf( form.getFirstValue( "versionexpand" ) );
        }
        if ( form.getFirstValue( "packagingexpand" ) != null )
        {
            expandPackaging = Boolean.valueOf( form.getFirstValue( "packagingexpand" ) );
        }
        if ( form.getFirstValue( "classifierexpand" ) != null )
        {
            expandClassifier = Boolean.valueOf( form.getFirstValue( "classifierexpand" ) );
        }
        if ( form.getFirstValue( "collapseresults" ) != null )
        {
            collapseResults = Boolean.valueOf( form.getFirstValue( "collapseresults" ) );
        }

        IteratorSearchResponse searchResult = null;

        SearchResponse result = new SearchResponse();

        // doing "plain search"
        final int RETRIES = 3;

        int runCount = 0;

        while ( runCount < RETRIES )
        {
            try
            {
                searchResult =
                    searchByTerms( terms, getRepositoryId( request ), from, count == null ? 500 : count, exact,
                        expandVersion, expandPackaging, expandClassifier, collapseResults );

                // non-identify search happened
                boolean tooManyResults = searchResult.isHitLimitExceeded();

                result.setTooManyResults( tooManyResults );

                result.setTotalCount( searchResult.getTotalHits() );

                result.setFrom( from == null ? -1 : from.intValue() );

                result.setCount( count == null ? -1 : count );

                if ( tooManyResults )
                {
                    result.setData( new ArrayList<NexusArtifact>() );
                }
                else
                {
                    result.setData( new ArrayList<NexusArtifact>( ai2NaColl( request, searchResult.getResults() ) ) );

                    // if we had collapseResults ON, and the totalHits are larger than actual (filtered) results, and
                    // the actual result count is below COLLAPSE_OVERRIDE_TRESHOLD,
                    // and full result set is smaller than HIT_LIMIT
                    // then repeat without collapse
                    if ( collapseResults && result.getData().size() < searchResult.getTotalHits()
                        && result.getData().size() < COLLAPSE_OVERRIDE_TRESHOLD
                        && searchResult.getTotalHits() < HIT_LIMIT )
                    {
                        collapseResults = false;

                        continue;
                    }
                }

                // we came here, so we break the while-loop, we got what we need
                break;
            }
            catch ( NoSuchRepositoryException e )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Repository with ID='"
                    + getRepositoryId( request ) + "' does not exists!", e );
            }
            catch ( AlreadyClosedException e )
            {
                getLogger().info(
                    "*** NexusIndexer bug, we got AlreadyClosedException that should never happen with ReadOnly IndexReaders! Please put Nexus into DEBUG log mode and report this issue together with the stack trace!" );

                if ( getLogger().isDebugEnabled() )
                {
                    // just keep it silent (DEBUG)
                    getLogger().debug( "Got AlreadyClosedException exception!", e );
                }

                result.setData( null );
            }

            runCount++;
        }

        if ( result.getData() == null )
        {
            result.setTooManyResults( true );

            result.setData( new ArrayList<NexusArtifact>() );

            getLogger().info(
                "Nexus BUG: Was unable to perform search " + RETRIES
                    + " times, giving up, and lying about TooManyResults." );
        }

        return result;
    }

    private IteratorSearchResponse searchByTerms( final Map<String, String> terms, final String repositoryId,
                                                  final Integer from, final Integer count, final Boolean exact,
                                                  final Boolean expandVersion, final Boolean expandPackaging,
                                                  final Boolean expandClassifier, final Boolean collapseResults )
        throws NoSuchRepositoryException, ResourceException
    {
        for ( Searcher searcher : m_searchers )
        {
            if ( searcher.canHandle( terms ) )
            {
                SearchType searchType = searcher.getDefaultSearchType();

                if ( exact != null )
                {
                    if ( exact )
                    {
                        searchType = SearchType.EXACT;
                    }
                    else
                    {
                        searchType = SearchType.SCORED;
                    }
                }

                List<ArtifactInfoFilter> filters = new ArrayList<ArtifactInfoFilter>();

                boolean uniqueRGA = false;

                if ( collapseResults )
                {
                    // filters should affect only Keyword and GAVSearch!
                    // TODO: maybe we should left this to the given Searcher implementation to handle (like kw and gav
                    // searcer is)
                    // Downside would be that REST query params would be too far away from incoming call (too spread)
                    if ( searcher instanceof KeywordSearcher || searcher instanceof MavenCoordinatesSearcher )
                    {
                        UniqueArtifactFilterPostprocessor filter = new UniqueArtifactFilterPostprocessor();

                        filter.addField( MAVEN.GROUP_ID );
                        filter.addField( MAVEN.ARTIFACT_ID );

                        if ( Boolean.TRUE.equals( expandVersion ) )
                        {
                            filter.addField( MAVEN.VERSION );
                        }
                        if ( Boolean.TRUE.equals( expandPackaging ) )
                        {
                            filter.addField( MAVEN.PACKAGING );
                        }
                        if ( Boolean.TRUE.equals( expandClassifier ) )
                        {
                            filter.addField( MAVEN.CLASSIFIER );
                        }

                        filters.add( filter );

                        uniqueRGA = true;
                    }
                }

                final IteratorSearchResponse searchResponse =
                    searcher.flatIteratorSearch( terms, repositoryId, from, count, HIT_LIMIT, uniqueRGA, searchType,
                        filters );

                if ( searchResponse != null )
                {
                    if ( searchResponse.isHitLimitExceeded() )
                    {
                        return IteratorSearchResponse.TOO_MANY_HITS_ITERATOR_SEARCH_RESPONSE;
                    }
                    else if ( collapseResults && searchResponse.getTotalHits() < COLLAPSE_OVERRIDE_TRESHOLD )
                    {
                        // this was a "collapsed" search (probably initiated by UI), and we have less then treshold hits
                        // override collapse
                        return searchByTerms( terms, repositoryId, from, count, exact, expandVersion, expandPackaging,
                            expandClassifier, false );
                    }
                    else
                    {
                        return searchResponse;
                    }
                }
            }
        }

        throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Requested search query is not supported" );
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        ReindexTask task = getNexusScheduler().createTaskInstance( ReindexTask.class );

        task.setRepositoryId( getRepositoryId( request ) );

        task.setRepositoryGroupId( getRepositoryGroupId( request ) );

        task.setResourceStorePath( getResourceStorePath( request ) );

        task.setFullReindex( getIsFullReindex() );

        handleDelete( task, request );
    }

    protected abstract boolean getIsFullReindex();

    protected NexusScheduler getNexusScheduler()
    {
        return nexusScheduler;
    }

    protected String getRepositoryId( Request request )
        throws ResourceException
    {
        String repoId = null;

        if ( ( request.getAttributes().containsKey( DOMAIN ) && request.getAttributes().containsKey( TARGET_ID ) )
            && DOMAIN_REPOSITORIES.equals( request.getAttributes().get( DOMAIN ) ) )
        {
            repoId = request.getAttributes().get( TARGET_ID ).toString();

            try
            {
                // simply to throw NoSuchRepository exception
                getRepositoryRegistry().getRepositoryWithFacet( repoId, Repository.class );
            }
            catch ( NoSuchRepositoryException e )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository not found!", e );
            }
        }

        return repoId;
    }

    protected String getRepositoryGroupId( Request request )
        throws ResourceException
    {
        String groupId = null;

        if ( ( request.getAttributes().containsKey( DOMAIN ) && request.getAttributes().containsKey( TARGET_ID ) )
            && DOMAIN_REPO_GROUPS.equals( request.getAttributes().get( DOMAIN ) ) )
        {
            groupId = request.getAttributes().get( TARGET_ID ).toString();

            try
            {
                // simply to throw NoSuchRepository exception
                getRepositoryRegistry().getRepositoryWithFacet( groupId, GroupRepository.class );
            }
            catch ( NoSuchRepositoryException e )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository group not found!", e );
            }
        }

        return groupId;
    }

    protected String getResourceStorePath( Request request )
        throws ResourceException
    {
        String path = null;

        if ( getRepositoryId( request ) != null || getRepositoryGroupId( request ) != null )
        {
            path = request.getResourceRef().getRemainingPart();

            // get rid of query part
            if ( path.contains( "?" ) )
            {
                path = path.substring( 0, path.indexOf( '?' ) );
            }

            // get rid of reference part
            if ( path.contains( "#" ) )
            {
                path = path.substring( 0, path.indexOf( '#' ) );
            }

            if ( StringUtils.isEmpty( path ) )
            {
                path = "/";
            }
        }
        return path;
    }

    public void handleDelete( NexusTask<?> task, Request request )
        throws ResourceException
    {
        try
        {
            // check reposes
            if ( getRepositoryGroupId( request ) != null )
            {
                getRepositoryRegistry().getRepositoryWithFacet( getRepositoryGroupId( request ), GroupRepository.class );
            }
            else if ( getRepositoryId( request ) != null )
            {
                try
                {
                    getRepositoryRegistry().getRepository( getRepositoryId( request ) );
                }
                catch ( NoSuchRepositoryException e )
                {
                    getRepositoryRegistry().getRepositoryWithFacet( getRepositoryId( request ), ShadowRepository.class );
                }
            }

            getNexusScheduler().submit( "Internal", task );

            throw new ResourceException( Status.SUCCESS_NO_CONTENT );
        }
        catch ( RejectedExecutionException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_CONFLICT, e.getMessage() );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
    }

}
