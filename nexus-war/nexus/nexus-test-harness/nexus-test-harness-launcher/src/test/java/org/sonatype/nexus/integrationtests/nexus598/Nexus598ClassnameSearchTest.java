package org.sonatype.nexus.integrationtests.nexus598;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.SearchMessageUtil;

/**
 * Test class name search functionality.
 */
public class Nexus598ClassnameSearchTest
    extends AbstractNexusIntegrationTest
{
    public Nexus598ClassnameSearchTest()
    {
//        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void searchDeployedArtifact()
        throws Exception
    {
        List<NexusArtifact> artifacts =
            SearchMessageUtil.searchClassname( "org.sonatype.nexus.test.classnamesearch.ClassnameSearchTestHelper" );
        Assert.assertFalse( "Nexus598 artifact was not found", artifacts.isEmpty() );
    }

    @Test
    public void unqualifiedSearchDeployedArtifact()
        throws Exception
    {
        List<NexusArtifact> artifacts = SearchMessageUtil.searchClassname( "ClassnameSearchTestHelper" );
        Assert.assertFalse( "Nexus598 artifact was not found", artifacts.isEmpty() );
    }

    @Test
    public void searchUnexistentClass()
        throws Exception
    {
        List<NexusArtifact> artifacts =
            SearchMessageUtil.searchClassname( "I.hope.this.class.name.is.not.available.at.nexus.repo.for.test.issue.Nexus598" );
        Assert.assertTrue( "The search found something, but it shouldn't.", artifacts.isEmpty() );
    }

}
