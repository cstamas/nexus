package org.sonatype.nexus.integrationtests.nexus532;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.NexusConfigUtil;

/**
 * CRUD tests for XML request/response.
 */
public class Nexus532GroupsCrudXmlTests extends AbstractNexusIntegrationTest
{
    protected GroupMessageUtil messageUtil;

    public Nexus532GroupsCrudXmlTests()
    {
        this.messageUtil =
            new GroupMessageUtil( this.getXMLXStream(),
                                  MediaType.APPLICATION_XML );
    }

    @Test
    public void createGroupTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "createTestGroup" );
        resource.setName( "createTestGroup" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );


        // this also validates
        this.messageUtil.createGroup( resource );
    }

    @Test
    public void readTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "readTestGroup" );
        resource.setName( "readTestGroup" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );


        // this also validates
        this.messageUtil.createGroup( resource );

        RepositoryGroupResource responseRepo = this.messageUtil.getGroup( resource.getId() );

        // validate they are the same
        this.messageUtil.validateResourceResponse( resource, responseRepo );

    }

    @Test
    public void updateTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "updateTestGroup" );
        resource.setName( "updateTestGroup" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );


        // this also validates
        resource = this.messageUtil.createGroup( resource );

        // udpdate the group
        member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo2" );
        resource.addRepository( member );

        this.messageUtil.updateGroup( resource );

    }

    @Test
    public void deleteTest()
        throws IOException
    {
        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "deleteTestGroup" );
        resource.setName( "deleteTestGroup" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );


        // this also validates
        resource = this.messageUtil.createGroup( resource );

        // now delete it...
        // use the new ID
        Response response = this.messageUtil.sendMessage( Method.DELETE, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not delete Repository: " + response.getStatus() );
        }
        Assert.assertNull( NexusConfigUtil.getRepo( resource.getId() ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void listTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "listTestGroup" );
        resource.setName( "listTestGroup" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );


        // this also validates
        resource = this.messageUtil.createGroup( resource );

        // now get the lists
        List<RepositoryGroupListResource> groups = this.messageUtil.getList();

        for ( Iterator<RepositoryGroupListResource> iter = groups.iterator(); iter.hasNext(); )
        {
            RepositoryGroupListResource group = iter.next();
            CRepositoryGroup cGroup = NexusConfigUtil.getGroup( group.getId() );

            Assert.assertNotNull( "CRepositoryGroup", cGroup );

            this.messageUtil.validateRepoLists( group.getRepositories(), cGroup.getRepositories() );
        }
    }
}
