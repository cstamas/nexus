/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tam�s Cserven�k (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.Query;

public abstract class AbstractRepoNexusIndexerTest
    extends AbstractNexusIndexerTest
{

    protected File repo = new File( getBasedir(), "src/test/repo" );

    public void testRootGroups()
        throws Exception
    {
        Set<String> rootGroups = nexusIndexer.getRootGroups( context );
        assertEquals( rootGroups.toString(), 7, rootGroups.size() );

        assertGroup( 2, "qdox", context );

        assertGroup( 1, "junit", context );

        assertGroup( 6, "commons-logging", context );

        assertGroup( 1, "regexp", context );

        assertGroup( 1, "commons-cli", context );

        assertGroup( 12, "org", context );

        assertGroup( 4, "org.slf4j", context );

        assertGroup( 3, "org.testng", context );

        assertGroup( 2, "org.apache", context );

        assertGroup( 1, "org.apache.directory", context );
        assertGroup( 1, "org.apache.directory.server", context );

        assertGroup( 1, "org.apache.maven", context );
        assertGroup( 1, "org.apache.maven.plugins", context );
        assertGroup( 0, "org.apache.maven.plugins.maven-core-it-plugin", context );
    }

    public void testSearchFlat()
        throws Exception
    {
        Query q = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "qdox" );

        Collection<ArtifactInfo> r = nexusIndexer.searchFlat( q );

        assertEquals( 2, r.size() );

        List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( r );

        assertEquals( 2, list.size() );

        ArtifactInfo ai = list.get( 0 );

        assertEquals( "1.6.1", ai.version );

        ai = list.get( 1 );

        assertEquals( "1.5", ai.version );

        assertEquals( "test", ai.repository );

    }

    public void testSearchGrouped()
        throws Exception
    {
        // ----------------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------------
        Query q = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "qdox" );

        Map<String, ArtifactInfoGroup> r = nexusIndexer.searchGrouped( new GAGrouping(), q );

        assertEquals( 1, r.size() );

        ArtifactInfoGroup ig = r.values().iterator().next();

        assertEquals( "qdox : qdox", ig.getGroupKey() );

        assertEquals( 2, ig.getArtifactInfos().size() );

        List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( ig.getArtifactInfos() );

        assertEquals( 2, list.size() );

        ArtifactInfo ai = list.get( 0 );

        assertEquals( "1.6.1", ai.version );

        ai = list.get( 1 );

        assertEquals( "1.5", ai.version );

        assertEquals( "test", ai.repository );
    }

    public void testSearchGroupedProblematicNames()
        throws Exception
    {

        // ----------------------------------------------------------------------------
        // Artifacts with "problematic" names
        // ----------------------------------------------------------------------------

        // "-" in the name
        Query q = nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-logg*" );

        Map<String, ArtifactInfoGroup> r = nexusIndexer.searchGrouped( new GAGrouping(), q );

        assertEquals( 1, r.size() );

        ArtifactInfoGroup ig = r.values().iterator().next();

        assertEquals( "commons-logging : commons-logging", ig.getGroupKey() );

        assertEquals( 6, ig.getArtifactInfos().size() );

        // numbers and "-" in the name
        q = nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "jcl104-over-slf4*" );

        r = nexusIndexer.searchGrouped( new GAGrouping(), q );

        assertEquals( 1, r.size() );

        ig = r.values().iterator().next();

        assertEquals( 1, ig.getArtifactInfos().size() );

        assertEquals( "org.slf4j : jcl104-over-slf4j", ig.getGroupKey() );
    }

    public void testConstructQuery()
    {
        Query q = nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "jcl104-over-slf4*" );

        assertEquals( "+a:jcl104 +a:over +a:slf4*", q.toString() );

    }

    public void testIdentify()
        throws Exception
    {
        ArtifactInfo ai = nexusIndexer.identify( ArtifactInfo.SHA1, "4d2db265eddf1576cb9d896abc90c7ba46b48d87" );

        assertNotNull( ai );

        assertEquals( "qdox", ai.groupId );

        assertEquals( "qdox", ai.artifactId );

        assertEquals( "1.5", ai.version );

        // Using a file

        File artifact = new File( repo, "qdox/qdox/1.5/qdox-1.5.jar" );

        ai = nexusIndexer.identify( artifact );

        assertNotNull( ai );

        assertEquals( "qdox", ai.groupId );

        assertEquals( "qdox", ai.artifactId );

        assertEquals( "1.5", ai.version );
    }

}
