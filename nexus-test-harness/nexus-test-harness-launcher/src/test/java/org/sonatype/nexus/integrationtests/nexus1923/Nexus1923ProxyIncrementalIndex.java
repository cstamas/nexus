package org.sonatype.nexus.integrationtests.nexus1923;

import java.io.File;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus1923ProxyIncrementalIndex
    extends AbstractNexus1923
{
    public Nexus1923ProxyIncrementalIndex()
        throws Exception
    {
        super();
    }
    
    @Test
    public void validateIncrementalIndexesDownloaded()
        throws Exception
    {
        File hostedRepoStorageDirectory = getHostedRepositoryStorageDirectory();
        
        //First create our hosted repository
        createHostedRepository();
        //And hosted repository task
        String hostedReindexId = createHostedReindexTask();
        //index hosted repo
        FileUtils.copyDirectoryStructure( getTestFile( FIRST_ARTIFACT ), 
            hostedRepoStorageDirectory );
        reindexHostedRepository( hostedReindexId );
        
        //Now create our proxy repository
        createProxyRepository();
        
        //will download the initial index because repo has download remote set to true
        TaskScheduleUtil.waitForTasks();
        
        //Now make sure that the search is properly working
        searchForArtifactInProxyIndex( FIRST_ARTIFACT, true );
        searchForArtifactInProxyIndex( SECOND_ARTIFACT, false );
        searchForArtifactInProxyIndex( THIRD_ARTIFACT, false );
        searchForArtifactInProxyIndex( FOURTH_ARTIFACT, false );
        searchForArtifactInProxyIndex( FIFTH_ARTIFACT, false );
        
        //Now add items to hosted, and reindex to create incremental chunk
        FileUtils.copyDirectoryStructure( getTestFile( SECOND_ARTIFACT ), 
            hostedRepoStorageDirectory );
        reindexHostedRepository( hostedReindexId );
        
        //now download via the proxy repo
        String proxyReindexId = createProxyReindexTask();
        reindexProxyRepository( proxyReindexId );
        
        //validate that after reindex is done we have an incremental chunk of our own in the proxy repo
        Assert.assertTrue( getProxyRepositoryIndex().exists() );
        Assert.assertTrue( getProxyRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getProxyRepositoryIndexIncrement( "2" ).exists() );
        
        //Now make sure that the search is properly working
        searchForArtifactInProxyIndex( FIRST_ARTIFACT, true );
        searchForArtifactInProxyIndex( SECOND_ARTIFACT, true );
        searchForArtifactInProxyIndex( THIRD_ARTIFACT, false );
        searchForArtifactInProxyIndex( FOURTH_ARTIFACT, false );
        searchForArtifactInProxyIndex( FIFTH_ARTIFACT, false );
        
        // Now make the hosted have 3 more index chunks
        FileUtils.copyDirectoryStructure( getTestFile( THIRD_ARTIFACT ), 
            hostedRepoStorageDirectory );
        reindexHostedRepository( hostedReindexId );
        
        FileUtils.copyDirectoryStructure( getTestFile( FOURTH_ARTIFACT ), 
            hostedRepoStorageDirectory );
        reindexHostedRepository( hostedReindexId );
        
        reindexProxyRepository( proxyReindexId );
        
        //validate that after reindex is done we have an incremental chunk of our own in the proxy repo
        //of course only 2 indexes, as these published indexes should NOT line up 1 to 1 with the hosted repo
        Assert.assertTrue( getProxyRepositoryIndex().exists() );
        Assert.assertTrue( getProxyRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertTrue( getProxyRepositoryIndexIncrement( "2" ).exists() );
        Assert.assertFalse( getProxyRepositoryIndexIncrement( "3" ).exists() );
        
        //Now make sure that the search is properly working
        searchForArtifactInProxyIndex( FIRST_ARTIFACT, true );
        searchForArtifactInProxyIndex( SECOND_ARTIFACT, true );
        searchForArtifactInProxyIndex( THIRD_ARTIFACT, true );
        searchForArtifactInProxyIndex( FOURTH_ARTIFACT, true );
        searchForArtifactInProxyIndex( FIFTH_ARTIFACT, false );
        
        //Now delete some items and put some back
        deleteAllNonHiddenContent( getHostedRepositoryStorageDirectory() );
        deleteAllNonHiddenContent( getProxyRepositoryStorageDirectory() );
        FileUtils.copyDirectoryStructure( getTestFile( FIRST_ARTIFACT ), 
            hostedRepoStorageDirectory );
        FileUtils.copyDirectoryStructure( getTestFile( SECOND_ARTIFACT ), 
            hostedRepoStorageDirectory );
        
        //Reindex
        reindexHostedRepository( hostedReindexId );
        
        //reindex proxy and make sure we cant search for the now missing items
        reindexProxyRepository( proxyReindexId );
        
        //Make sure the indexes exist, and that a new one has been created with
        //the deletes
        //TODO SKIP FOR NOW, BUT NEED TO FIX
        /*
        Assert.assertTrue( getProxyRepositoryIndex().exists() );
        Assert.assertTrue( getProxyRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertTrue( getProxyRepositoryIndexIncrement( "2" ).exists() );
        Assert.assertTrue( getProxyRepositoryIndexIncrement( "3" ).exists() );
        Assert.assertFalse( getProxyRepositoryIndexIncrement( "4" ).exists() );
        */
        
        searchForArtifactInProxyIndex( FIRST_ARTIFACT, true );
        searchForArtifactInProxyIndex( SECOND_ARTIFACT, true );
        searchForArtifactInProxyIndex( THIRD_ARTIFACT, false );
        searchForArtifactInProxyIndex( FOURTH_ARTIFACT, false );
        searchForArtifactInProxyIndex( FIFTH_ARTIFACT, false );
    }
}
