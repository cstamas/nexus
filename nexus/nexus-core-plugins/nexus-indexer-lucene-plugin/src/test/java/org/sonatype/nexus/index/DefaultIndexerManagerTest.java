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
package org.sonatype.nexus.index;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.MAVEN;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.templates.repository.maven.Maven2ProxyRepositoryTemplate;

public class DefaultIndexerManagerTest
    extends AbstractIndexerManagerTest
{

    private Nexus nexus;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexus = lookup( Nexus.class );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        nexus = null;

        super.tearDown();
    }

    public void testRepoReindex()
        throws Exception
    {
        fillInRepo();

        indexerManager.reindexAllRepositories( "/", true );

        searchFor( "org.sonatype.nexus", 10 );

        assertTemporatyContexts( releases );
    }

    public void testRepoKeywordSearch()
        throws Exception
    {
        fillInRepo();

        indexerManager.reindexAllRepositories( "/", true );

        searchForKeywordNG( "org.sonatype.nexus", 10 );

        assertTemporatyContexts( releases );
    }

    public void testRepoSha1Search()
        throws Exception
    {
        fillInRepo();

        indexerManager.reindexAllRepositories( "/", true );

        // org.sonatype.nexus : nexus-indexer : 1.0-beta-4
        // sha1: 86e12071021fa0be4ec809d4d2e08f07b80d4877

        ArtifactInfo ai = indexerManager.identifyArtifact( MAVEN.SHA1, "86e12071021fa0be4ec809d4d2e08f07b80d4877" );

        assertNotNull( "The artifact has to be found!", ai );

        IteratorSearchResponse response;

        // this will be EXACT search, since we gave full SHA1 checksum of 40 chars
        response =
            indexerManager.searchArtifactSha1ChecksumIterator( "86e12071021fa0be4ec809d4d2e08f07b80d4877", null, null,
                null, null, null );

        assertEquals( "There should be one hit!", 1, response.getTotalHits() );
        
        response.close();

        // this will be SCORED search, since we have just part of the SHA1 checksum
        response = indexerManager.searchArtifactSha1ChecksumIterator( "86e12071021", null, null, null, null, null );

        assertEquals( "There should be still one hit!", 1, response.getTotalHits() );
        
        response.close();
    }

    public void testInvalidRemoteUrl()
        throws Exception
    {
        Maven2ProxyRepositoryTemplate t =
            (Maven2ProxyRepositoryTemplate) nexus.getRepositoryTemplateById( "default_proxy_snapshot" );
        t.getConfigurableRepository().setId( "invalidUrlRepo" );
        ProxyRepository r = t.create().adaptToFacet( ProxyRepository.class );
        r.setRemoteUrl( "http://repository.sonatyp.org/content/repositories/snapshots" );

        nexusConfiguration.saveConfiguration();

        indexerManager.reindexRepository( "/", r.getId(), true );
    }
}
