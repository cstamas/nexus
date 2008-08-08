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

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;

public class RepoChecksumPolicyTest
    extends AbstractProxyTestEnvironment
{

    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );
        return jettyTestsuiteEnvironmentBuilder;
    }

    protected M2Repository getRepository()
        throws Exception
    {
        return (M2Repository) getRepositoryRegistry().getRepository( "checksumTestRepo" );
    }

    public StorageFileItem requestWithPolicy( ChecksumPolicy policy, ResourceStoreRequest request )
        throws Exception
    {
        M2Repository repo = getRepository();

        repo.setChecksumPolicy( policy );

        StorageFileItem item = (StorageFileItem) repo.retrieveItem( request );

        return item;
    }

    public void testPolicyIgnore()
        throws Exception
    {
        ChecksumPolicy policy = ChecksumPolicy.IGNORE;

        // it should simply pull all four without problem
        StorageFileItem file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // IGNORE: the req ignores checksum
        assertFalse( getRepository()
            .getLocalStorage().containsItem(
                new RepositoryItemUid(
                    getRepository(),
                    "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar.sha1" ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // IGNORE: the req ignores checksum
        assertFalse( getRepository().getLocalStorage().containsItem(
            new RepositoryItemUid( getRepository(), "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar.md5" ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // IGNORE: the req ignores checksum
        assertFalse( getRepository()
            .getLocalStorage().containsItem(
                new RepositoryItemUid(
                    getRepository(),
                    "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar.sha1" ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // IGNORE: the req ignores checksum
        assertFalse( getRepository().getLocalStorage().containsItem(
            new RepositoryItemUid(
                getRepository(),
                "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar.sha1" ) ) );
    }

    public void testPolicyWarn()
        throws Exception
    {
        ChecksumPolicy policy = ChecksumPolicy.WARN;

        // it should simply pull all four without problem
        StorageFileItem file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // WARN: the req implicitly gets the "best" checksum available implicitly
        assertTrue( getRepository()
            .getLocalStorage().containsItem(
                new RepositoryItemUid(
                    getRepository(),
                    "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar.sha1" ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // WARN: the req implicitly gets the "best" checksum available implicitly
        assertTrue( getRepository().getLocalStorage().containsItem(
            new RepositoryItemUid( getRepository(), "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar.md5" ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // WARN: the req implicitly gets the "best" checksum available implicitly
        assertFalse( getRepository()
            .getLocalStorage().containsItem(
                new RepositoryItemUid(
                    getRepository(),
                    "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar.sha1" ) ) );
        assertFalse( getRepository()
            .getLocalStorage().containsItem(
                new RepositoryItemUid(
                    getRepository(),
                    "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar.md5" ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // WARN: the req implicitly gets the "best" checksum available implicitly
        assertTrue( getRepository().getLocalStorage().containsItem(
            new RepositoryItemUid(
                getRepository(),
                "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar.sha1" ) ) );
    }

    public void testPolicyStrictIfExists()
        throws Exception
    {
        ChecksumPolicy policy = ChecksumPolicy.STRICT_IF_EXISTS;

        // it should simply pull all four without problem
        StorageFileItem file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // STRICT_IF_EXISTS: the req implicitly gets the "best" checksum available implicitly
        assertTrue( getRepository()
            .getLocalStorage().containsItem(
                new RepositoryItemUid(
                    getRepository(),
                    "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar.sha1" ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // STRICT_IF_EXISTS: the req implicitly gets the "best" checksum available implicitly
        assertTrue( getRepository().getLocalStorage().containsItem(
            new RepositoryItemUid( getRepository(), "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar.md5" ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // STRICT_IF_EXISTS: the req implicitly gets the "best" checksum available implicitly
        assertFalse( getRepository()
            .getLocalStorage().containsItem(
                new RepositoryItemUid(
                    getRepository(),
                    "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar.sha1" ) ) );
        assertFalse( getRepository()
            .getLocalStorage().containsItem(
                new RepositoryItemUid(
                    getRepository(),
                    "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar.md5" ) ) );

        try
        {
            file = requestWithPolicy( policy, new ResourceStoreRequest(
                "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar",
                false ) );
            checkForFileAndMatchContents( file );
            // STRICT_IF_EXISTS: the req implicitly gets the "best" checksum available implicitly

            fail();
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }
        assertFalse( getRepository().getLocalStorage().containsItem(
            new RepositoryItemUid(
                getRepository(),
                "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar.sha1" ) ) );
    }

    public void testPolicyStrict()
        throws Exception
    {
        ChecksumPolicy policy = ChecksumPolicy.STRICT;

        // it should simply pull all four without problem
        StorageFileItem file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // STRICT: the req implicitly gets the "best" checksum available implicitly
        assertTrue( getRepository()
            .getLocalStorage().containsItem(
                new RepositoryItemUid(
                    getRepository(),
                    "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar.sha1" ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // STRICT: the req implicitly gets the "best" checksum available implicitly
        assertTrue( getRepository().getLocalStorage().containsItem(
            new RepositoryItemUid( getRepository(), "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar.md5" ) ) );

        try
        {
            file = requestWithPolicy( policy, new ResourceStoreRequest(
                "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar",
                false ) );
            checkForFileAndMatchContents( file );
            // STRICT: the req implicitly gets the "best" checksum available implicitly

            fail();
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }
        assertFalse( getRepository()
            .getLocalStorage().containsItem(
                new RepositoryItemUid(
                    getRepository(),
                    "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar.sha1" ) ) );
        assertFalse( getRepository()
            .getLocalStorage().containsItem(
                new RepositoryItemUid(
                    getRepository(),
                    "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar.md5" ) ) );

        try
        {
            file = requestWithPolicy( policy, new ResourceStoreRequest(
                "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar",
                false ) );
            checkForFileAndMatchContents( file );
            // STRICT: the req implicitly gets the "best" checksum available implicitly

            fail();
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }
        assertFalse( getRepository().getLocalStorage().containsItem(
            new RepositoryItemUid(
                getRepository(),
                "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar.sha1" ) ) );
    }

}
