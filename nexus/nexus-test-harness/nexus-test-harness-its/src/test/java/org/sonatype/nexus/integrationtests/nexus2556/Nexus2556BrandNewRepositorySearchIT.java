package org.sonatype.nexus.integrationtests.nexus2556;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.maven.index.artifact.Gav;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus2556BrandNewRepositorySearchIT
    extends AbstractNexusIntegrationTest
{

    private RepositoryMessageUtil repoUtil;

    public Nexus2556BrandNewRepositorySearchIT()
    {

    }

    @BeforeClass
    public void init()
        throws ComponentLookupException
    {
        this.repoUtil =
            new RepositoryMessageUtil( this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML,
                                       getRepositoryTypeRegistry() );
    }

    @Test
    public void hostedTest()
        throws IOException, Exception
    {
        String repoId = "nexus2556-hosted";
        RepositoryResource repo = new RepositoryResource();
        repo.setProvider( "maven2" );
        repo.setFormat( "maven2" );
        repo.setRepoPolicy( "release" );
        repo.setChecksumPolicy( "ignore" );
        repo.setBrowseable( false );

        repo.setId( repoId );
        repo.setName( repoId );
        repo.setRepoType( "hosted" );
        repo.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );
        repo.setDownloadRemoteIndexes( true );
        repo.setBrowseable( true );
        repo.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        repo.setChecksumPolicy( ChecksumPolicy.IGNORE.name() );

        repo.setIndexable( true ); // being sure!!!
        repoUtil.createRepository( repo );

        repo = (RepositoryResource) repoUtil.getRepository( repoId );
        Assert.assertTrue( repo.isIndexable() );
        
        TaskScheduleUtil.waitForAllTasksToStop();
        getEventInspectorsUtil().waitForCalmPeriod();

        Gav gav = GavUtil.newGav( "nexus2556", "artifact", "1.0" );
        getDeployUtils().deployUsingGavWithRest( repoId, gav, getTestFile( "artifact.jar" ) );

        getEventInspectorsUtil().waitForCalmPeriod();

        List<NexusArtifact> result = getSearchMessageUtil().searchForGav( gav, repoId );
        Assert.assertEquals( result.size(), 1, "Results: \n" + XStreamFactory.getXmlXStream().toXML( result ) );

        result = getSearchMessageUtil().searchFor( Collections.singletonMap( "q", "nexus2556" ), repoId );
        Assert.assertEquals( result.size(), 1, "Results: \n" + XStreamFactory.getXmlXStream().toXML( result ) );
    }

}
