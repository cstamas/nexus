package org.sonatype.nexus.integrationtests.nexus688;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class Nexus688ReindexOnRepoAdd
    extends AbstractNexusIntegrationTest
{

    private RepositoryMessageUtil messageUtil = new RepositoryMessageUtil(this.getXMLXStream(), MediaType.APPLICATION_XML);

    private static final String INDEX_FILE = ".index/nexus-maven-repository-index.zip";
    
    private static final int SLEEP_TIME = 1000;

    @Test
    public void repoTestIndexable()
        throws IOException, InterruptedException
    {

        // create a repo
        RepositoryResource resource = new RepositoryResource();

        resource.setId( "nexus688-repoTestIndexable" );
        resource.setRepoType( "hosted" );
        resource.setName( "Create Test Repo" );
        resource.setFormat( "maven2" );
        resource.setRepoPolicy( "release" );
        resource.setChecksumPolicy( "ignore" );
        resource.setBrowseable( true );
        resource.setIndexable( true );
        resource.setAllowWrite( true );

        // this also validates
        this.messageUtil.createRepository( resource );
        
        Thread.sleep( SLEEP_TIME );
        
        // check to see if it has an index to download
        File indexFile = this.downloadIndexFromRepository( resource.getId() );
        
        // if the above line didn't throw a FileNotFound, we are good to go, but check anyway.
        Assert.assertNotNull( "Downloaded index file was null.", indexFile );
        Assert.assertTrue( "Downloaded index does not exists", indexFile.exists() );
    }
    
    @Test
    public void repoTestNotIndexable()
        throws IOException, InterruptedException
    {

        // create a repo
        RepositoryResource resource = new RepositoryResource();

        resource.setId( "nexus688-repoTestNotIndexable" );
        resource.setRepoType( "hosted" );
        resource.setName( "Create Test Repo" );
        resource.setFormat( "maven2" );
        resource.setRepoPolicy( "release" );
        resource.setChecksumPolicy( "ignore" );
        resource.setBrowseable( true );
        resource.setIndexable( false );
        resource.setAllowWrite( true );

        // this also validates
        this.messageUtil.createRepository( resource );
        
        Thread.sleep( SLEEP_TIME );
        
        
        // check to see if it has an index to download        
        try
        {
            this.downloadIndexFromRepository( resource.getId() );
            Assert.fail( "Expected a 404, FileNotFoundException." );
        }
        catch(FileNotFoundException e)
        {
          // expected 404
        }
    }
    
    @Test
    public void proxyRepoTestIndexableWithInvalidURL()
        throws IOException, InterruptedException
    {

        // create a repo
        RepositoryProxyResource resource = new RepositoryProxyResource();

        resource.setId( "nexus688-proxyRepoTestIndexableWithInvalidURL" );
        resource.setRepoType( "proxy" );
        resource.setName( "Create Test Repo" );
        resource.setFormat( "maven2" );
        resource.setRepoPolicy( "release" );
        resource.setChecksumPolicy( "ignore" );
        resource.setBrowseable( true );
        resource.setIndexable( true );
        resource.setAllowWrite( true );
        
        RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
        remoteStorage.setRemoteStorageUrl( "http://INVALID-URL" );
        resource.setRemoteStorage( remoteStorage );

        // this also validates
        this.messageUtil.createRepository( resource );
        
        Thread.sleep( SLEEP_TIME );
        

        // check to see if it has an index to download
        File indexFile = this.downloadIndexFromRepository( resource.getId() );
        
        // if the above line didn't throw a FileNotFound, we are good to go, but check anyway.
        Assert.assertNotNull( "Downloaded index file was null.", indexFile );
        Assert.assertTrue( "Downloaded index does not exists", indexFile.exists() );
    }
    
    @Test
    public void proxyRepoTestIndexable()
        throws IOException, InterruptedException
    {

        // create a repo
        RepositoryProxyResource resource = new RepositoryProxyResource();

        resource.setId( "nexus688-proxyRepoTestIndexable" );
        resource.setRepoType( "proxy" );
        resource.setName( "Create Test Repo" );
        resource.setFormat( "maven2" );
        resource.setRepoPolicy( "release" );
        resource.setChecksumPolicy( "ignore" );
        resource.setBrowseable( true );
        resource.setIndexable( true );
        resource.setAllowWrite( true );
        
        RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
        remoteStorage.setRemoteStorageUrl( "http://INVALID-URL" );
        resource.setRemoteStorage( remoteStorage );
        
        // this also validates
        this.messageUtil.createRepository( resource );
        
        Thread.sleep( SLEEP_TIME );
        

        // check to see if it has an index to download
        File indexFile = this.downloadIndexFromRepository( resource.getId() );
        
        // if the above line didn't throw a FileNotFound, we are good to go, but check anyway.
        Assert.assertNotNull( "Downloaded index file was null.", indexFile );
        Assert.assertTrue( "Downloaded index does not exists", indexFile.exists() );
    }
    
    @Test
    public void proxyRepoTestNotIndexable()
        throws IOException, InterruptedException
    {

        // create a repo
        RepositoryProxyResource resource = new RepositoryProxyResource();

        resource.setId( "nexus688-proxyRepoTestNotIndexable" );
        resource.setRepoType( "proxy" );
        resource.setName( "Create Test Repo" );
        resource.setFormat( "maven2" );
        resource.setRepoPolicy( "release" );
        resource.setChecksumPolicy( "ignore" );
        resource.setBrowseable( true );
        resource.setIndexable( false );
        resource.setAllowWrite( true );
        
        RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
        remoteStorage.setRemoteStorageUrl( "http://INVALID-URL" );
        resource.setRemoteStorage( remoteStorage );

        // this also validates
        this.messageUtil.createRepository( resource );
        
        Thread.sleep( SLEEP_TIME );

        // check to see if it has an index to download
        try
        {
            this.downloadIndexFromRepository( resource.getId() );
            Assert.fail( "Expected a 404, FileNotFoundException." );
        }
        catch(FileNotFoundException e)
        {
          // expected 404
        }
    }
    
    private File downloadIndexFromRepository( String repoId ) throws MalformedURLException, IOException
    {
        return this.downloadFile( new URL( this.getRepositoryUrl( repoId ) + INDEX_FILE ), "target/downloads/index.zip" );
    }

}
