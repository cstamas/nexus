/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.nexus531;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.rest.model.RepositoryResource;

/**
 * Test Repo CRUD privileges.
 */
public class Nexus531RepositoryCrudPermissionIT extends AbstractPrivilegeTest
{


    @Test
    public void testCreatePermission()
        throws IOException
    {
        this.giveUserPrivilege( TEST_USER_NAME, "repository-all" );
        
        RepositoryResource repo = new RepositoryResource();
        repo.setId( "testCreatePermission" );
        repo.setName( "testCreatePermission" );
        repo.setRepoType( "hosted" );
        repo.setProvider( "maven1" );
        // format is neglected by server from now on, provider is the new guy in the town
        repo.setFormat( "maven1" );
        repo.setRepoPolicy( RepositoryPolicy.SNAPSHOT.name() );
        repo.setChecksumPolicy( "IGNORE" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        Response response = this.repoUtil.sendMessage( Method.POST, repo );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // now give create
        this.giveUserPrivilege( TEST_USER_NAME, "5" );

        // now.... it should work...
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.repoUtil.sendMessage( Method.POST, repo );
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        repo = (RepositoryResource) this.repoUtil.getRepository( repo.getId() );

        // read should succeed (inherited)
        response = this.repoUtil.sendMessage( Method.GET, repo );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.repoUtil.sendMessage( Method.PUT, repo );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // delete should fail
        response = this.repoUtil.sendMessage( Method.DELETE, repo );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

    }

    @Test
    public void testUpdatePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        
        this.giveUserPrivilege( TEST_USER_NAME, "repository-all" );

        RepositoryResource repo = new RepositoryResource();
        repo.setId( "testUpdatePermission" );
        repo.setName( "testUpdatePermission" );
        repo.setRepoType( "hosted" );
        repo.setProvider( "maven1" );
        // format is neglected by server from now on, provider is the new guy in the town
        repo.setFormat( "maven1" );
        repo.setRepoPolicy( RepositoryPolicy.SNAPSHOT.name() );
        repo.setChecksumPolicy( "IGNORE" );

        Response response = this.repoUtil.sendMessage( Method.POST, repo );
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        repo = (RepositoryResource) this.repoUtil.getRepository( repo.getId() );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update repo
        repo.setName( "tesUpdatePermission2" );
        response = this.repoUtil.sendMessage( Method.PUT, repo );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // now give update
        this.giveUserPrivilege( TEST_USER_NAME, "7" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        response = this.repoUtil.sendMessage( Method.PUT, repo );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // read should succeed (inherited)
        response = this.repoUtil.sendMessage( Method.GET, repo );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.repoUtil.sendMessage( Method.POST, repo );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // delete should fail
        response = this.repoUtil.sendMessage( Method.DELETE, repo );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

    }

    @Test
    public void testReadPermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        
        this.giveUserPrivilege( TEST_USER_NAME, "repository-all" );

        RepositoryResource repo = new RepositoryResource();
        repo.setId( "testReadPermission" );
        repo.setName( "testReadPermission" );
        repo.setRepoType( "hosted" );
        repo.setProvider( "maven1" );
        // format is neglected by server from now on, provider is the new guy in the town
        repo.setFormat( "maven1" );
        repo.setRepoPolicy( RepositoryPolicy.SNAPSHOT.name() );
        repo.setChecksumPolicy( "IGNORE" );

        Response response = this.repoUtil.sendMessage( Method.POST, repo );
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        repo = (RepositoryResource) this.repoUtil.getRepository( repo.getId() );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update repo
        repo.setName( "tesUpdatePermission2" );
        response = this.repoUtil.sendMessage( Method.PUT, repo );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // now give read
        this.giveUserPrivilege( TEST_USER_NAME, "6" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // read should fail
        response = this.repoUtil.sendMessage( Method.GET, repo );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.repoUtil.sendMessage( Method.POST, repo );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // delete should fail
        response = this.repoUtil.sendMessage( Method.PUT, repo );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

     // should work now...
        response = this.repoUtil.sendMessage( Method.DELETE, repo );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

    }


    @Test
    public void testDeletePermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        
        this.giveUserPrivilege( TEST_USER_NAME, "repository-all" );

        RepositoryResource repo = new RepositoryResource();
        repo.setId( "testDeletePermission" );
        repo.setName( "testDeletePermission" );
        repo.setRepoType( "hosted" );
        repo.setProvider( "maven1" );
        // format is neglected by server from now on, provider is the new guy in the town
        repo.setFormat( "maven1" );
        repo.setRepoPolicy( RepositoryPolicy.SNAPSHOT.name() );
        repo.setChecksumPolicy( "IGNORE" );

        Response response = this.repoUtil.sendMessage( Method.POST, repo );
        Assert.assertEquals( "Response status: ", 201, response.getStatus().getCode() );
        repo = (RepositoryResource) this.repoUtil.getRepository( repo.getId() );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // update repo
        repo.setName( "tesUpdatePermission2" );
        response = this.repoUtil.sendMessage( Method.DELETE, repo );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        // now give delete
        this.giveUserPrivilege( TEST_USER_NAME, "8" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // read should succeed (inherited)
        response = this.repoUtil.sendMessage( Method.GET, repo );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // update should fail
        response = this.repoUtil.sendMessage( Method.POST, repo );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

        // delete should fail
        response = this.repoUtil.sendMessage( Method.PUT, repo );
        Assert.assertEquals( "Response status: ", 403, response.getStatus().getCode() );

     // should work now...
        response = this.repoUtil.sendMessage( Method.DELETE, repo );
        Assert.assertEquals( "Response status: ", 204, response.getStatus().getCode() );

    }

}
