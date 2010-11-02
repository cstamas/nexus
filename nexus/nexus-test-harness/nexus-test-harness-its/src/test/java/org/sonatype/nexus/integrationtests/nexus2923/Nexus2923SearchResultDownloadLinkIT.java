package org.sonatype.nexus.integrationtests.nexus2923;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.util.StringUtils;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.UpdateIndexTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test the 'pom' and 'artifact' download link in the search result panel
 * 
 * @author juven
 */
public class Nexus2923SearchResultDownloadLinkIT
    extends AbstractNexusIntegrationTest
{
     public Nexus2923SearchResultDownloadLinkIT()
    {
        super( "nexus2923" );
    }

    @Override
    public void runOnce()
        throws Exception
    {
        File testRepo = new File( nexusWorkDir, "storage/" + this.getTestRepositoryId() );
        File testFiles = getTestFile( "repo" );
        FileUtils.copyDirectory( testFiles, testRepo );

        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( "repositoryOrGroupId" );
        prop.setValue( "repo_" + this.getTestRepositoryId() );

        TaskScheduleUtil.runTask( UpdateIndexTaskDescriptor.ID, prop );
    }

    @Test
    public void testDownnloadLinks()
        throws Exception
    {
        List<NexusArtifact> artifacts = getSearchMessageUtil().searchFor( "xbean-server" );
        Assert.assertEquals( artifacts.size(), 3, "The artifact should be indexed" );

        for ( NexusArtifact artifact : artifacts )
        {
            if ( StringUtils.isNotEmpty( artifact.getPomLink() ) )
            {
                assertLinkAvailable( artifact.getPomLink() );
            }

            if ( StringUtils.isNotEmpty( artifact.getArtifactLink() ) )
            {
                assertLinkAvailable( artifact.getArtifactLink() );
            }
        }
    }

    private void assertLinkAvailable( String link )
        throws Exception
    {
        Response response = RequestFacade.sendMessage( new URL( link ), Method.GET, null );

        Assert.assertEquals(
            response.getStatus().getCode(),
            301,
            "Invalid link: '" + link + "' response code is '" + response.getStatus().getCode() + "'" );
    }
}
