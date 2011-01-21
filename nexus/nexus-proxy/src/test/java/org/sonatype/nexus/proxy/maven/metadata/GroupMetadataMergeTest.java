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
package org.sonatype.nexus.proxy.maven.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.digest.Md5Digester;
import org.codehaus.plexus.digest.Sha1Digester;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.M2TestsuiteEnvironmentBuilder;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataBuilder;
import org.sonatype.nexus.proxy.maven.metadata.operations.ModelVersionUtility;

public class GroupMetadataMergeTest
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

    public void testGMerge()
        throws Exception
    {
        String mdPath = "/md-merge/g/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( 4, md.getPlugins().size() );
        assertEquals( "core-it", ( md.getPlugins().get( 0 ) ).getPrefix() );
        assertEquals( "resources", ( md.getPlugins().get( 1 ) ).getPrefix() );
        assertEquals( "site", ( md.getPlugins().get( 2 ) ).getPrefix() );
        assertEquals( "surefire-report", ( md.getPlugins().get( 3 ) ).getPrefix() );
    }

    public void testGAMerge()
        throws Exception
    {
        String mdPath = "/md-merge/ga/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( "org.sonatype.nexus", md.getGroupId() );
        assertEquals( "nexus", md.getArtifactId() );

        assertEquals( "1.4.0-SNAPSHOT", md.getVersioning().getLatest() );
        assertEquals( "1.3.4", md.getVersioning().getRelease() );
        String[] versions =
            { "1.2.1", "1.3.0", "1.3.1-SNAPSHOT", "1.3.1", "1.3.2", "1.3.3-SNAPSHOT", "1.3.3", "1.3.4",
                "1.4.0-SNAPSHOT" };
        assertEquals( Arrays.asList( versions ), md.getVersioning().getVersions() );
        assertEquals( "20090620231210", md.getVersioning().getLastUpdated() );
    }

    public void testGAMerge2()
        throws Exception
    {
        String mdPath = "/md-merge/ga2/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( "org.distribution.core", md.getGroupId() );
        assertEquals( "core", md.getArtifactId() );

        assertEquals( "2.3.0.5-SNAPSHOT", md.getVersioning().getLatest() );
        assertEquals( "2.3.0.4", md.getVersioning().getRelease() );
        String[] versions =
            { "2.3.0.2-SNAPSHOT", "2.3.0.2", "2.3.0.3-SNAPSHOT", "2.3.0.3", "2.3.0.4-SNAPSHOT", "2.3.0.4",
                "2.3.0.5-SNAPSHOT" };
        assertEquals( Arrays.asList( versions ), md.getVersioning().getVersions() );
        assertEquals( "20091124120836", md.getVersioning().getLastUpdated() );
    }

    /**
     * Merge 3 GA maven-metadata.xml
     * 
     * @throws Exception
     */
    public void testGA3Merge()
        throws Exception
    {
        String mdPath = "/md-merge/ga3/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( "org.sonatype.nexus", md.getGroupId() );
        assertEquals( "nexus", md.getArtifactId() );

        assertEquals( "1.4.1-SNAPSHOT", md.getVersioning().getLatest() );
        assertEquals( "1.3.4", md.getVersioning().getRelease() );
        String[] versions =
            { "1.2.1", "1.3.0", "1.3.1-SNAPSHOT", "1.3.1", "1.3.2", "1.3.3-SNAPSHOT", "1.3.3", "1.3.4",
                "1.4.0-SNAPSHOT", "1.4.1-SNAPSHOT" };
        assertEquals( Arrays.asList( versions ), md.getVersioning().getVersions() );
        assertEquals( "20090720231210", md.getVersioning().getLastUpdated() );
    }

    public void testGA4Merge()
        throws Exception
    {
        String mdPath = "/md-merge/ga4/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( "xxx.distribution.core", md.getGroupId() );
        assertEquals( "core", md.getArtifactId() );

        assertEquals( "2.3.0.0.5-SNAPSHOT", md.getVersioning().getLatest() );
        assertEquals( "2.3.0.0.4", md.getVersioning().getRelease() );
        String[] versions =
            { "2.3.0.0.1-SNAPSHOT", "2.3.0.0.2-SNAPSHOT", "2.3.0.0.2", "2.3.0.0.3-SNAPSHOT", "2.3.0.0.3",
                "2.3.0.0.4-SNAPSHOT", "2.3.0.0.4", "2.3.0.0.5-SNAPSHOT" };
        assertEquals( Arrays.asList( versions ), md.getVersioning().getVersions() );
        assertEquals( "20091119113313", md.getVersioning().getLastUpdated() );
    }

    public void testGAVMerge()
        throws Exception
    {
        String mdPath = "/md-merge/gav/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( "org.sonatype.nexus", md.getGroupId() );
        assertEquals( "nexus", md.getArtifactId() );
        assertEquals( "1.3.4-SNAPSHOT", md.getVersion() );
        assertEquals( "20090527.162714", md.getVersioning().getSnapshot().getTimestamp() );
        assertEquals( 51, md.getVersioning().getSnapshot().getBuildNumber() );
        assertEquals( "20090527162714", md.getVersioning().getLastUpdated() );
    }

    public void testGAVMergeWithNewBuildNumberAndOldTimestamp()
        throws Exception
    {
        String mdPath = "/md-merge/gav2/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( "org.sonatype.nexus", md.getGroupId() );
        assertEquals( "nexus", md.getArtifactId() );
        assertEquals( "1.3.4-SNAPSHOT", md.getVersion() );
        assertEquals( "20090331.203702", md.getVersioning().getSnapshot().getTimestamp() );
        assertEquals( 2, md.getVersioning().getSnapshot().getBuildNumber() );
        assertEquals( "20090331203702", md.getVersioning().getLastUpdated() );
    }

    public void testChecksum()
        throws Exception
    {
        String mdPath = "/md-merge/checksum/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        File mdFile = File.createTempFile( "metadata", "tmp" );
        saveItemToFile( ( (StorageFileItem) item ), mdFile );

        StorageItem md5Item =
            getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath + ".md5", false ) );
        StorageItem sha1Item =
            getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath + ".sha1", false ) );

        String md5Hash = contentAsString( md5Item );
        String sha1Hash = contentAsString( sha1Item );

        Md5Digester md5Digester = new Md5Digester();
        md5Digester.verify( mdFile, md5Hash );
        Sha1Digester sha1Digester = new Sha1Digester();
        sha1Digester.verify( mdFile, sha1Hash );
    }

    public void testConflictMerge()
        throws Exception
    {
        String mdPath = "/md-merge/conflict/maven-metadata.xml";

        try
        {
            getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );

            fail( "Should not be able to retrieve the maven-metadata.xml, since the merge should fail caused by incompatible artifactId." );
        }
        catch ( StorageException e )
        {
            getLogger().info( e.getMessage() );
            getLogger().info( e.getCause().getMessage() );
        }
    }

    public void testReleasePolicy()
        throws Exception
    {
        String mdPath = "/md-merge/release/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( "1.3.2", md.getVersioning().getLatest() );
        assertEquals( "1.3.2", md.getVersioning().getRelease() );
        String[] versions = { "1.3.0", "1.3.2" };
        assertEquals( Arrays.asList( versions ), md.getVersioning().getVersions() );
        assertEquals( "20090720231210", md.getVersioning().getLastUpdated() );
    }

    public void testSnapshotPolicy()
        throws Exception
    {
        String mdPath = "/md-merge/snapshot/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( "1.4.1-SNAPSHOT", md.getVersioning().getLatest() );
        assertNull( md.getVersioning().getRelease() );
        String[] versions = { "1.4.1-SNAPSHOT" };
        assertEquals( Arrays.asList( versions ), md.getVersioning().getVersions() );
        assertEquals( "20090720231210", md.getVersioning().getLastUpdated() );
    }

    public void testV100V100MdMerge()
        throws Exception
    {
        // net/test/tamas/test/3.0-SNAPSHOT
        String mdPath = "/net/test/tamas/test/3.0-SNAPSHOT/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( ModelVersionUtility.Version.V100, ModelVersionUtility.getModelVersion( md ) );
        assertEquals( 0, md.getVersioning().getSnapshotVersions().size() );
    }

    public void testV110V110MdMerge()
        throws Exception
    {
        // net/test/tamas/test/4.0-SNAPSHOT
        String mdPath = "/net/test/tamas/test/4.0-SNAPSHOT/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( ModelVersionUtility.Version.V110, ModelVersionUtility.getModelVersion( md ) );
        assertEquals( 3, md.getVersioning().getSnapshotVersions().size() );
    }

    public void testV110V100MdMergeWithV110Newer()
        throws Exception
    {
        // net/test/tamas/test/1.0-SNAPSHOT
        String mdPath = "/net/test/tamas/test/1.0-SNAPSHOT/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( ModelVersionUtility.Version.V110, ModelVersionUtility.getModelVersion( md ) );
        assertEquals( 3, md.getVersioning().getSnapshotVersions().size() );
        assertEquals( "20110121213648", md.getVersioning().getSnapshotVersions().get( 0 ).getUpdated() );
    }

    public void testV110V100MdMergeWithV100Newer()
        throws Exception
    {
        // net/test/tamas/test/2.0-SNAPSHOT
        String mdPath = "/net/test/tamas/test/2.0-SNAPSHOT/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( ModelVersionUtility.Version.V110, ModelVersionUtility.getModelVersion( md ) );
        assertEquals( 3, md.getVersioning().getSnapshotVersions().size() );
        assertEquals( "20110122213648", md.getVersioning().getSnapshotVersions().get( 0 ).getUpdated() );

    }

    protected Metadata parseMetadata( File file )
        throws Exception
    {
        InputStream in = null;

        try
        {
            in = new FileInputStream( file );

            return MetadataBuilder.read( in );
        }
        finally
        {
            if ( in != null )
            {
                in.close();
            }
        }
    }

    protected Metadata parseMetadata( StorageFileItem item )
        throws Exception
    {
        InputStream in = null;

        try
        {
            in = item.getInputStream();

            return MetadataBuilder.read( in );
        }
        finally
        {
            if ( in != null )
            {
                in.close();
            }
        }
    }

}
