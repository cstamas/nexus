package org.sonatype.nexus.integrationtests.proxy.nexus262;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;

import org.sonatype.nexus.integrationtests.proxy.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.testng.annotations.Test;


/**
 * One step above the Sample Test, this one adds a 'remote repository': <a href='https://docs.sonatype.com/display/NX/Nexus+Test-Harness'>Nexus Test-Harness</a>
 */
public class Nexus262SimpleProxyTest extends AbstractNexusProxyIntegrationTest
{

    public Nexus262SimpleProxyTest()
    {
        super( "release-proxy-repo-1" );
    }
    
    @Test
    public void downloadFromProxy() throws IOException
    {
        File localFile = this.getLocalFile( "release-proxy-repo-1", "simple.artifact", "simpleXMLArtifact", "1.0.0", "xml" );
                                                                                              
        log.debug( "localFile: "+ localFile.getAbsolutePath() );
        
        File artifact = this.downloadArtifact( "simple.artifact", "simpleXMLArtifact", "1.0.0", "xml", null, "target/downloads" );
        
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( artifact, localFile ) );
    }

}
