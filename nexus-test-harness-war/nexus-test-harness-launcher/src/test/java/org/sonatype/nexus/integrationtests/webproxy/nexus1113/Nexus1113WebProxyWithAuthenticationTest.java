package org.sonatype.nexus.integrationtests.webproxy.nexus1113;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.webproxy.AbstractNexusWebProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class Nexus1113WebProxyWithAuthenticationTest
    extends AbstractNexusWebProxyIntegrationTest
{

    @Override
    public void startWebProxy()
        throws Exception
    {
        super.startWebProxy();
        server.getProxyServlet().setUseAuthentication( true );
        server.getProxyServlet().getAuthentications().put( "admin", "123" );
    }

    @Test
    public void downloadArtifactOverWebProxy()
        throws Exception
    {
        File pomFile = this.getLocalFile( "release-proxy-repo-1", "nexus1113", "artifact", "1.0", "pom" );
        File pomArtifact = this.downloadArtifact( "nexus1113", "artifact", "1.0", "pom", null, "target/downloads/nexus1113" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( pomArtifact, pomFile ) );

        File jarFile = this.getLocalFile( "release-proxy-repo-1", "nexus1113", "artifact", "1.0", "jar" );
        File jarArtifact = this.downloadArtifact( "nexus1113", "artifact", "1.0", "jar", null, "target/downloads/nexus1113" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( jarArtifact, jarFile ) );

        String artifactUrl = baseProxyURL + "release-proxy-repo-1/nexus1113/artifact/1.0/artifact-1.0.jar";
        Assert.assertTrue( "Proxy was not accessed", server.getAccessedUris().contains( artifactUrl ) );
    }


    @Override
    public void stopWebProxy()
        throws Exception
    {
        server.getProxyServlet().setUseAuthentication( false );
        server.getProxyServlet().setAuthentications( null );
        super.stopWebProxy();
    }
}
