/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.M2Repository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public class M2RepositoryTest
    extends M2ResourceStoreTest
{

    protected static final String SPOOF_RELEASE = "/spoof/spoof/1.0/spoof-1.0.txt";

    protected static final String SPOOF_SNAPSHOT = "/spoof/spoof/1.0-SNAPSHOT/spoof-1.0-SNAPSHOT.txt";

    @Override
    protected String getItemPath()
    {
        return "/activemq/activemq-core/1.2/activemq-core-1.2.jar";
    }

    @Override
    protected ResourceStore getResourceStore()
        throws NoSuchRepositoryException
    {
        Repository repo1 = getRepositoryRegistry().getRepository( "repo1" );

        repo1.setAllowWrite( true );

        return repo1;
    }

    public void testPoliciesWithRetrieve()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        // a "release"
        repository.setShouldServeReleases( true );
        repository.setShouldServeSnapshots( false );

        StorageItem item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_RELEASE, false ) );
        checkForFileAndMatchContents( item );

        try
        {
            item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_SNAPSHOT, false ) );

            fail( "Should not be able to get snapshot from release repo" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }

        // reset NFC
        repository.clearCaches( "/" );

        // a "snapshot"
        repository.setShouldServeReleases( false );
        repository.setShouldServeSnapshots( true );

        item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_SNAPSHOT, false ) );
        checkForFileAndMatchContents( item );

        try
        {
            item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_RELEASE, false ) );

            fail( "Should not be able to get release from snapshot repo" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }
    }

    public void testPoliciesWithStore()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        // a "release"
        repository.setShouldServeReleases( true );
        repository.setShouldServeSnapshots( false );

        DefaultStorageFileItem item = new DefaultStorageFileItem(
            repository,
            SPOOF_RELEASE,
            true,
            true,
            new ByteArrayInputStream( SPOOF_RELEASE.getBytes() ) );

        repository.storeItem( item );

        try
        {
            item = new DefaultStorageFileItem( repository, SPOOF_SNAPSHOT, true, true, new ByteArrayInputStream(
                SPOOF_SNAPSHOT.getBytes() ) );

            repository.storeItem( item );

            fail( "Should not be able to store snapshot to release repo" );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // good
        }

        // reset NFC
        repository.clearCaches( "/" );

        // a "snapshot"
        repository.setShouldServeReleases( false );
        repository.setShouldServeSnapshots( true );

        item = new DefaultStorageFileItem( repository, SPOOF_SNAPSHOT, true, true, new ByteArrayInputStream(
            SPOOF_SNAPSHOT.getBytes() ) );

        repository.storeItem( item );

        try
        {
            item = new DefaultStorageFileItem( repository, SPOOF_RELEASE, true, true, new ByteArrayInputStream(
                SPOOF_RELEASE.getBytes() ) );

            repository.storeItem( item );

            fail( "Should not be able to store release to snapshot repo" );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // good
        }
    }

    public void testShouldServeByPolicies()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        RepositoryItemUid releasePom = new RepositoryItemUid(
            repository,
            "/org/codehaus/plexus/plexus-container-default/1.0-alpha-40/plexus-container-default-1.0-alpha-40.pom" );
        RepositoryItemUid releaseArtifact = new RepositoryItemUid(
            repository,
            "/org/codehaus/plexus/plexus-container-default/1.0-alpha-40/plexus-container-default-1.0-alpha-40.jar" );
        RepositoryItemUid snapshotPom = new RepositoryItemUid(
            repository,
            "/org/codehaus/plexus/plexus-container-default/1.0-alpha-41-SNAPSHOT/plexus-container-default-1.0-alpha-41-20071205.190351-1.pom" );
        RepositoryItemUid snapshotArtifact = new RepositoryItemUid(
            repository,
            "/org/codehaus/plexus/plexus-container-default/1.0-alpha-41-SNAPSHOT/plexus-container-default-1.0-alpha-41-20071205.190351-1.jar" );
        RepositoryItemUid metadata1 = new RepositoryItemUid(
            repository,
            "/org/codehaus/plexus/plexus-container-default/maven-metadata.xml" );
        RepositoryItemUid metadataR = new RepositoryItemUid(
            repository,
            "/org/codehaus/plexus/plexus-container-default/1.0-alpha-40/maven-metadata.xml" );
        RepositoryItemUid metadataS = new RepositoryItemUid(
            repository,
            "/org/codehaus/plexus/plexus-container-default/1.0-alpha-41-SNAPSHOT/maven-metadata.xml" );
        RepositoryItemUid someDirectory = new RepositoryItemUid( repository, "/classworlds/" );
        RepositoryItemUid anyNonArtifactFile = new RepositoryItemUid( repository, "/any/file.txt" );

        // it is equiv of repo type: RELEASE
        repository.setShouldServeReleases( true );
        repository.setShouldServeSnapshots( false );
        assertEquals( true, repository.shouldServeByPolicies( releasePom ) );
        assertEquals( true, repository.shouldServeByPolicies( releaseArtifact ) );
        assertEquals( false, repository.shouldServeByPolicies( snapshotPom ) );
        assertEquals( false, repository.shouldServeByPolicies( snapshotArtifact ) );
        assertEquals( true, repository.shouldServeByPolicies( metadata1 ) );
        assertEquals( true, repository.shouldServeByPolicies( metadataR ) );
        assertEquals( false, repository.shouldServeByPolicies( metadataS ) );
        assertEquals( true, repository.shouldServeByPolicies( someDirectory ) );
        assertEquals( true, repository.shouldServeByPolicies( anyNonArtifactFile ) );

        // it is equiv of repo type: SNAPSHOT
        repository.setShouldServeReleases( false );
        repository.setShouldServeSnapshots( true );
        assertEquals( false, repository.shouldServeByPolicies( releasePom ) );
        assertEquals( false, repository.shouldServeByPolicies( releaseArtifact ) );
        assertEquals( true, repository.shouldServeByPolicies( snapshotPom ) );
        assertEquals( true, repository.shouldServeByPolicies( snapshotArtifact ) );
        assertEquals( true, repository.shouldServeByPolicies( metadata1 ) );
        assertEquals( true, repository.shouldServeByPolicies( metadataR ) );
        assertEquals( true, repository.shouldServeByPolicies( metadataS ) );
        assertEquals( true, repository.shouldServeByPolicies( someDirectory ) );
        assertEquals( true, repository.shouldServeByPolicies( anyNonArtifactFile ) );

        // for complete tests, but an impossible to configure
        repository.setShouldServeReleases( true );
        repository.setShouldServeSnapshots( true );
        assertEquals( true, repository.shouldServeByPolicies( releasePom ) );
        assertEquals( true, repository.shouldServeByPolicies( releaseArtifact ) );
        assertEquals( true, repository.shouldServeByPolicies( snapshotPom ) );
        assertEquals( true, repository.shouldServeByPolicies( snapshotArtifact ) );
        assertEquals( true, repository.shouldServeByPolicies( metadata1 ) );
        assertEquals( true, repository.shouldServeByPolicies( metadataR ) );
        assertEquals( true, repository.shouldServeByPolicies( metadataS ) );
        assertEquals( true, repository.shouldServeByPolicies( someDirectory ) );
        assertEquals( true, repository.shouldServeByPolicies( anyNonArtifactFile ) );

        // for complete tests, but an impossible to configure
        repository.setShouldServeReleases( false );
        repository.setShouldServeSnapshots( false );
        assertEquals( false, repository.shouldServeByPolicies( releasePom ) );
        assertEquals( false, repository.shouldServeByPolicies( releaseArtifact ) );
        assertEquals( false, repository.shouldServeByPolicies( snapshotPom ) );
        assertEquals( false, repository.shouldServeByPolicies( snapshotArtifact ) );
        assertEquals( true, repository.shouldServeByPolicies( metadata1 ) );
        assertEquals( true, repository.shouldServeByPolicies( metadataR ) );
        assertEquals( false, repository.shouldServeByPolicies( metadataS ) );
        assertEquals( true, repository.shouldServeByPolicies( someDirectory ) );
        assertEquals( true, repository.shouldServeByPolicies( anyNonArtifactFile ) );
    }

    public void testGetLatestVersionSimple()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        List<String> versions = new ArrayList<String>();
        versions.add( "1.0.0" );
        versions.add( "1.0.1" );
        versions.add( "1.0.2" );
        versions.add( "1.1.2" );
        assertEquals( "1.1.2", repository.getLatestVersion( versions ) );
    }

    public void testGetLatestVersionClassifiers()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        List<String> versions = new ArrayList<String>();
        versions.add( "1.0-alpha-19" );
        versions.add( "1.0-alpha-9-stable-1" );
        versions.add( "1.0-alpha-20" );
        versions.add( "1.0-alpha-21" );
        versions.add( "1.0-alpha-22" );
        versions.add( "1.0-alpha-40" );
        assertEquals( "1.0-alpha-40", repository.getLatestVersion( versions ) );
    }

    public void testIsSnapshot()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        assertEquals( false, repository.isSnapshot( "1.0.0" ) );
        assertEquals( true, repository.isSnapshot( "1.0.0-SNAPSHOT" ) );
        assertEquals( false, repository.isSnapshot( "1.0-alpha-25" ) );
        assertEquals( true, repository.isSnapshot( "1.0-alpha-25-20070518.002146-2" ) );
    }
}
