package org.sonatype.nexus.integrationtests.webproxy.nexus1146;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.maven.it.Verifier;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.webproxy.AbstractNexusWebProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.TestProperties;

public class Nexus1146RepositoryOverProxyTest
    extends AbstractNexusWebProxyIntegrationTest
{

    @Test
    public void downloadArtifactOverWebProxy()
        throws Exception
    {
        File pomFile = this.getLocalFile( "release-proxy-repo-1", "nexus1146", "artifact", "1.0", "pom" );
        File pomArtifact = this.downloadArtifact( "nexus1146", "artifact", "1.0", "pom", null, "target/downloads" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( pomArtifact, pomFile ) );

        File jarFile = this.getLocalFile( "release-proxy-repo-1", "nexus1146", "artifact", "1.0", "jar" );
        File jarArtifact = this.downloadArtifact( "nexus1146", "artifact", "1.0", "jar", null, "target/downloads" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( jarArtifact, jarFile ) );

        String artifactUrl = baseProxyURL + "release-proxy-repo-1/nexus1146/artifact/1.0/artifact-1.0.jar";
        Assert.assertTrue( "Proxy was not accessed", server.getAccessedUris().contains( artifactUrl ) );
    }

    @Test( expected = FileNotFoundException.class )
    public void unexistentArtifact()
        throws Exception
    {
        try
        {
            this.downloadArtifact( "nexus1146", "some-artifact-that-dont-exists", "4.8.15.16.23.42", "jar", null,
                                   "target/downloads" );
        }
        finally
        {
            String artifactUrl =
                baseProxyURL
                    + "release-proxy-repo-1/nexus1146/some-artifact-that-dont-exists/4.8.15.16.23.42/some-artifact-that-dont-exists-4.8.15.16.23.42.jar";
            Assert.assertTrue( "Proxy was not accessed", server.getAccessedUris().contains( artifactUrl ) );
        }
    }

    @Test
    public void proxyWithMaven()
        throws Exception
    {
        File mavenProject = getTestFile( "pom.xml" ).getParentFile();
        Verifier verifier = new Verifier( mavenProject.getAbsolutePath(), false );

        System.setProperty( "maven.home", TestProperties.getString( "maven.instance" ) );

        File mavenRepository = new File( TestProperties.getString( "maven.local.repo" ) );
        verifier.setLocalRepo( mavenRepository.getAbsolutePath() );

        verifier.resetStreams();

        List<String> options = new ArrayList<String>();
        options.add( "-X" );
        options.add( "-Dmaven.repo.local=" + mavenRepository.getAbsolutePath() );
        options.add( "-s " + getOverridableFile( "settings.xml" ) );
        verifier.setCliOptions( options );

        verifier.executeGoal( "dependency:resolve" );
        verifier.verifyErrorFreeLog();

        String artifactUrl = baseProxyURL + "release-proxy-repo-1/nexus1146/maven-artifact/1.0/maven-artifact-1.0.jar";
        Assert.assertTrue( "Proxy was not accessed", server.getAccessedUris().contains( artifactUrl ) );
    }

}
