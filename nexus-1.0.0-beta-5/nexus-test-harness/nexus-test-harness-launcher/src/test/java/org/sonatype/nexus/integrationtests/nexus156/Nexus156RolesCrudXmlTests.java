package org.sonatype.nexus.integrationtests.nexus156;

import java.io.IOException;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.RoleMessageUtil;

import com.thoughtworks.xstream.XStream;

public class Nexus156RolesCrudXmlTests
    extends Nexus156RolesCrudJsonTests
{

    public Nexus156RolesCrudXmlTests()
    {
        this.messageUtil = new RoleMessageUtil(XStreamInitializer.initialize( new XStream( ) ), MediaType.APPLICATION_XML );
    }
    
    @Test
    public void readTest()
        throws IOException
    {
        super.readTest();
    }
    
}
