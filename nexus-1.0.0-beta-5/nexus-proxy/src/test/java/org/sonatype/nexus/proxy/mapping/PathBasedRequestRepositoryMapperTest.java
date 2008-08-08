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
package org.sonatype.nexus.proxy.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.DummyRepository;
import org.sonatype.nexus.proxy.repository.Repository;

public class PathBasedRequestRepositoryMapperTest
    extends AbstractNexusTestEnvironment
{

    private ApplicationConfiguration applicationConfiguration;

    private RepositoryRegistry registry;

    private Repository repoA;

    private Repository repoB;

    private Repository repoC;

    private Repository repoD;

    private Repository repoE;

    private Repository repoF;

    protected PathBasedRequestRepositoryMapper prepare( Map<String, String[]> inclusions,
        Map<String, String[]> exclusions, Map<String, String[]> blockings )
        throws Exception
    {
        applicationConfiguration = (ApplicationConfiguration) lookup( ApplicationConfiguration.ROLE );

        applicationConfiguration.getConfiguration().getRepositoryGrouping().getPathMappings().clear();

        registry = (RepositoryRegistry) lookup( RepositoryRegistry.ROLE );

        // clean this up?

        repoA = new DummyRepository();
        repoA.setId( "repoA" );
        repoB = new DummyRepository();
        repoB.setId( "repoB" );
        repoC = new DummyRepository();
        repoC.setId( "repoC" );
        repoD = new DummyRepository();
        repoD.setId( "repoD" );
        repoE = new DummyRepository();
        repoE.setId( "repoE" );
        repoF = new DummyRepository();
        repoF.setId( "repoF" );

        registry.addRepository( repoA );
        registry.addRepository( repoB );
        registry.addRepository( repoC );
        registry.addRepository( repoD );
        registry.addRepository( repoE );
        registry.addRepository( repoF );

        ArrayList<String> testgroup = new ArrayList<String>();
        testgroup.add( repoA.getId() );
        testgroup.add( repoB.getId() );
        testgroup.add( repoC.getId() );
        testgroup.add( repoD.getId() );
        testgroup.add( repoE.getId() );
        testgroup.add( repoF.getId() );

        registry.addRepositoryGroup( "test", testgroup );

        if ( inclusions != null )
        {
            for ( String key : inclusions.keySet() )
            {
                CGroupsSettingPathMappingItem item = new CGroupsSettingPathMappingItem();
                item.setId( "I" + key );
                item.setGroupId( CGroupsSettingPathMappingItem.ALL_GROUPS );
                item.setRoutePattern( key );
                item.setRouteType( CGroupsSettingPathMappingItem.INCLUSION_RULE_TYPE );
                item.setRepositories( Arrays.asList( inclusions.get( key ) ) );
                applicationConfiguration.getConfiguration().getRepositoryGrouping().addPathMapping( item );
            }
        }

        if ( exclusions != null )
        {
            for ( String key : exclusions.keySet() )
            {
                CGroupsSettingPathMappingItem item = new CGroupsSettingPathMappingItem();
                item.setId( "E" + key );
                item.setGroupId( CGroupsSettingPathMappingItem.ALL_GROUPS );
                item.setRoutePattern( key );
                item.setRouteType( CGroupsSettingPathMappingItem.EXCLUSION_RULE_TYPE );
                item.setRepositories( Arrays.asList( exclusions.get( key ) ) );
                applicationConfiguration.getConfiguration().getRepositoryGrouping().addPathMapping( item );
            }
        }

        if ( blockings != null )
        {
            for ( String key : blockings.keySet() )
            {
                CGroupsSettingPathMappingItem item = new CGroupsSettingPathMappingItem();
                item.setId( "B" + key );
                item.setGroupId( CGroupsSettingPathMappingItem.ALL_GROUPS );
                item.setRoutePattern( key );
                item.setRouteType( CGroupsSettingPathMappingItem.BLOCKING_RULE_TYPE );
                item.setRepositories( Arrays.asList( blockings.get( key ) ) );
                applicationConfiguration.getConfiguration().getRepositoryGrouping().addPathMapping( item );
            }
        }

        PathBasedRequestRepositoryMapper pm = (PathBasedRequestRepositoryMapper) lookup( RequestRepositoryMapper.ROLE );

        return pm;
    }

    public void testInclusionAndExclusion()
        throws Exception
    {
        HashMap<String, String[]> inclusions = new HashMap<String, String[]>();
        inclusions.put( "/a/b/.*", new String[] { "repoA", "repoB" } );
        inclusions.put( "/c/d/.*", new String[] { "repoC", "repoD" } );
        inclusions.put( "/all/.*", new String[] { "*" } );

        HashMap<String, String[]> exclusions = new HashMap<String, String[]>();
        exclusions.put( "/e/f/.*", new String[] { "*" } );

        PathBasedRequestRepositoryMapper pm = prepare( inclusions, exclusions, null );

        // using group to guarantee proper ordering
        List<ResourceStore> resolvedRepositories = new ArrayList<ResourceStore>();

        resolvedRepositories.addAll( registry.getRepositoryGroup( "test" ) );

        List<ResourceStore> mappedRepositories;

        ResourceStoreRequest request;

        request = new ResourceStoreRequest( "/a/b/something", true );
        mappedRepositories = pm.getMappedRepositories( registry, request, resolvedRepositories );
        assertEquals( 2, mappedRepositories.size() );
        assertTrue( mappedRepositories.get( 0 ).equals( repoA ) );
        assertTrue( mappedRepositories.get( 1 ).equals( repoB ) );

        request = new ResourceStoreRequest( "/e/f/should/not/return/any/repo", true );
        mappedRepositories = pm.getMappedRepositories( registry, request, resolvedRepositories );
        assertEquals( 0, mappedRepositories.size() );

        request = new ResourceStoreRequest( "/all/should/be/servicing", true );
        mappedRepositories = pm.getMappedRepositories( registry, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );

    }

    public void testInclusionAndExclusionKeepsGroupOrdering()
        throws Exception
    {
        HashMap<String, String[]> inclusions = new HashMap<String, String[]>();
        inclusions.put( "/a/b/.*", new String[] { "repoB", "repoA" } );
        inclusions.put( "/c/d/.*", new String[] { "repoD", "repoC" } );
        inclusions.put( "/all/.*", new String[] { "*" } );

        HashMap<String, String[]> exclusions = new HashMap<String, String[]>();
        exclusions.put( "/e/f/.*", new String[] { "repoE", "repoF" } );
        exclusions.put( "/e/f/all/.*", new String[] { "*" } );

        PathBasedRequestRepositoryMapper pm = prepare( inclusions, exclusions, null );

        // using group to guarantee proper ordering
        List<ResourceStore> resolvedRepositories = new ArrayList<ResourceStore>();

        resolvedRepositories.addAll( registry.getRepositoryGroup( "test" ) );

        List<ResourceStore> mappedRepositories;

        ResourceStoreRequest request;

        // /a/b inclusion hit, needed order: A, B
        request = new ResourceStoreRequest( "/a/b/something", true );
        mappedRepositories = pm.getMappedRepositories( registry, request, resolvedRepositories );
        assertEquals( 2, mappedRepositories.size() );
        assertTrue( mappedRepositories.get( 0 ).equals( repoA ) );
        assertTrue( mappedRepositories.get( 1 ).equals( repoB ) );

        // /e/f exclusion hit, needed order: A, B, C, D
        request = new ResourceStoreRequest( "/e/f/should/not/return/any/repo", true );
        mappedRepositories = pm.getMappedRepositories( registry, request, resolvedRepositories );
        assertEquals( 4, mappedRepositories.size() );
        assertTrue( mappedRepositories.get( 0 ).equals( repoA ) );
        assertTrue( mappedRepositories.get( 1 ).equals( repoB ) );
        assertTrue( mappedRepositories.get( 2 ).equals( repoC ) );
        assertTrue( mappedRepositories.get( 3 ).equals( repoD ) );

        request = new ResourceStoreRequest( "/all/should/be/servicing", true );
        mappedRepositories = pm.getMappedRepositories( registry, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );

    }

    /**
     * Empty rules are invalid, they are spitted out by validator anyway. This test is bad, and hence is turned off, but
     * it is left here for reference. (added 'dont' at the start)
     * 
     * @throws Exception
     */
    public void dontTestEmptyRules()
        throws Exception
    {
        HashMap<String, String[]> inclusions = new HashMap<String, String[]>();
        inclusions.put( "/empty/1/.*", new String[] { "" } );
        inclusions.put( "/empty/2/.*", new String[] { null } );
        inclusions.put( "/empty/5/.*", new String[] { null } );

        HashMap<String, String[]> exclusions = new HashMap<String, String[]>();
        exclusions.put( "/empty/5/.*", new String[] { "" } );
        exclusions.put( "/empty/6/.*", new String[] { "" } );
        exclusions.put( "/empty/7/.*", new String[] { null } );

        PathBasedRequestRepositoryMapper pm = prepare( inclusions, exclusions, null );

        // using group to guarantee proper ordering
        List<ResourceStore> resolvedRepositories = new ArrayList<ResourceStore>();

        resolvedRepositories.addAll( registry.getRepositoryGroup( "test" ) );

        List<ResourceStore> mappedRepositories;

        ResourceStoreRequest request;

        // empty inclusion, it should don't be acted upon
        request = new ResourceStoreRequest( "/empty/1/something", true );
        mappedRepositories = pm.getMappedRepositories( registry, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );

        // null inclusion, it should don't be acted upon
        request = new ResourceStoreRequest( "/empty/2/something", true );
        mappedRepositories = pm.getMappedRepositories( registry, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );

        request = new ResourceStoreRequest( "/empty/5/something", true );
        mappedRepositories = pm.getMappedRepositories( registry, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );

        request = new ResourceStoreRequest( "/empty/6/something", true );
        mappedRepositories = pm.getMappedRepositories( registry, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );

        request = new ResourceStoreRequest( "/empty/7/something", true );
        mappedRepositories = pm.getMappedRepositories( registry, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );
    }

    public void testBlockingRules()
        throws Exception
    {
        HashMap<String, String[]> blockings = new HashMap<String, String[]>();
        blockings.put( "/blocked/1/.*", new String[] { "" } );

        PathBasedRequestRepositoryMapper pm = prepare( null, null, blockings );

        // using group to guarantee proper ordering
        List<ResourceStore> resolvedRepositories = new ArrayList<ResourceStore>();

        resolvedRepositories.addAll( registry.getRepositoryGroup( "test" ) );

        List<ResourceStore> mappedRepositories;

        ResourceStoreRequest request;

        // empty inclusion, it should don't be acted upon
        request = new ResourceStoreRequest( "/blocked/1/something", true );
        mappedRepositories = pm.getMappedRepositories( registry, request, resolvedRepositories );
        assertEquals( 0, mappedRepositories.size() );

        // null inclusion, it should don't be acted upon
        request = new ResourceStoreRequest( "/dummy/2/something", true );
        mappedRepositories = pm.getMappedRepositories( registry, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );
    }

}
