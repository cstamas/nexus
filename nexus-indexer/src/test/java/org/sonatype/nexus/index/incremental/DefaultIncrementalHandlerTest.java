package org.sonatype.nexus.index.incremental;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.index.AbstractIndexCreatorHelper;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.packer.IndexPackingRequest;
import org.sonatype.nexus.index.updater.IndexUpdateRequest;
import org.sonatype.nexus.index.updater.ResourceFetcher;

public class DefaultIncrementalHandlerTest
    extends AbstractIndexCreatorHelper
{
    IncrementalHandler handler = null;

    NexusIndexer indexer = null;

    IndexingContext context = null;

    File indexDir = null;

    File repoDir = null;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        indexer = lookup( NexusIndexer.class );
        handler = lookup( IncrementalHandler.class );

        indexDir = new File( getBasedir(), "target/index/nexus-incremental-test" );
        repoDir = new File( getBasedir(), "target/repos/nexus-incremental-test" );
        FileUtils.deleteDirectory( indexDir );
        FileUtils.deleteDirectory( repoDir );

        context = indexer.addIndexingContext( "test", "test", repoDir, indexDir, null, null, DEFAULT_CREATORS );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        indexer.removeIndexingContext( context, true );
    }

    public void testUpdateInvalidProperties()
        throws Exception
    {
        Properties properties = new Properties();

        IndexPackingRequest request = new IndexPackingRequest( context, indexDir );

        // No properties definite fail
        assertNull( handler.getIncrementalUpdates( request, properties ) );

        properties.setProperty( IndexingContext.INDEX_TIMESTAMP, "junk" );

        // property set, but invalid
        assertNull( handler.getIncrementalUpdates( request, properties ) );

        properties.setProperty( IndexingContext.INDEX_TIMESTAMP, "19991112182432.432 -0600" );

        List<Integer> updates = handler.getIncrementalUpdates( request, properties );

        assertEquals( updates.size(), 0 );
    }

    public void testUpdateValid()
        throws Exception
    {
        Properties properties = new Properties();

        IndexPackingRequest request = new IndexPackingRequest( context, indexDir );

        properties.setProperty( IndexingContext.INDEX_TIMESTAMP, "19991112182432.432 -0600" );

        FileUtils.copyDirectoryStructure( new File( getBasedir(), "src/test/repo/ch" ), new File( repoDir, "ch" ) );

        indexer.scan( context );

        List<Integer> updates = handler.getIncrementalUpdates( request, properties );

        assertEquals( updates.size(), 1 );
    }

    public void testRemoteUpdatesInvalidProperties()
        throws Exception
    {
        // just a dummy fetcher, it's not used here anyway
        IndexUpdateRequest request = new IndexUpdateRequest( context, new ResourceFetcher()
        {
            public InputStream retrieve( String name )
                throws IOException, FileNotFoundException
            {
                // TODO Auto-generated method stub
                return null;
            }

            public void retrieve( String name, File targetFile )
                throws IOException, FileNotFoundException
            {
                // TODO Auto-generated method stub

            }

            public void disconnect()
                throws IOException
            {
                // TODO Auto-generated method stub

            }

            public void connect( String id, String url )
                throws IOException
            {
                // TODO Auto-generated method stub

            }
        } );

        Properties localProperties = new Properties();
        Properties remoteProperties = new Properties();

        List<String> filenames = handler.loadRemoteIncrementalUpdates( request, localProperties, remoteProperties );

        assertNull( filenames );
    }
}
