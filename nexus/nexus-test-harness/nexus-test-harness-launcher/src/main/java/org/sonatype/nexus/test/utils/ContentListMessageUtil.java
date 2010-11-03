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
package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.rest.model.ContentListResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.testng.Assert;

import com.thoughtworks.xstream.XStream;

public class ContentListMessageUtil
{

    private XStream xstream;

    private MediaType mediaType;

    public ContentListMessageUtil( XStream xstream, MediaType mediaType )
    {
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    protected Response getResponse( String repoId, String path, boolean isGroup )
        throws IOException
    {
        String groupOrRepoPart = isGroup ? "repo_groups/" : "repositories/";
        String uriPart = RequestFacade.SERVICE_LOCAL + groupOrRepoPart + repoId + "/content" + path;
        
        return RequestFacade.sendMessage( uriPart, Method.GET );
    }

    public List<ContentListResource> getContentListResource( String repoId, String path, boolean isGroup )
        throws IOException
    {
        Response response = this.getResponse( repoId, path, isGroup );

        String responeText = response.getEntity().getText();
        Assert.assertTrue(
            response.getStatus().isSuccess(),
            "Expected sucess: Status was: " + response.getStatus() + "\nResponse:\n" + responeText );

        XStreamRepresentation representation = new XStreamRepresentation( this.xstream, responeText, this.mediaType );
        ContentListResourceResponse listRepsonse = (ContentListResourceResponse) representation
            .getPayload( new ContentListResourceResponse() );

        return listRepsonse.getData();

    }
}
