package org.sonatype.nexus.integrationtests.nexus1560;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.TestContainer;

public class Nexus1560LegacyAllowGroupRulesTest
    extends AbstractLegacyRulesTest
{

    @Before
    public void init()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        addPriv( TEST_USER_NAME, NEXUS1560_GROUP + "-priv", "repositoryTarget", "1", null, NEXUS1560_GROUP, "read" );
    }

    @Test
    public void fromGroup()
        throws Exception
    {
        String downloadUrl = GROUP_REPOSITORY_RELATIVE_URL + NEXUS1560_GROUP + "/" + getRelitiveArtifactPath( gavArtifact1 );

        successDownload( downloadUrl );
    }

    @Test
    public void fromRepository()
        throws Exception
    {
        String downloadUrl =
            REPOSITORY_RELATIVE_URL + REPO_TEST_HARNESS_REPO + "/" + getRelitiveArtifactPath( gavArtifact1 );

        successDownload( downloadUrl );
    }

}
