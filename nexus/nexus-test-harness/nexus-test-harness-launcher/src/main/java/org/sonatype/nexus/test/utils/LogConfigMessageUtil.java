/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.test.utils;

import java.io.IOException;


import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.LogConfigResource;
import org.sonatype.nexus.rest.model.LogConfigResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.testng.Assert;

import com.thoughtworks.xstream.XStream;

/**
 * @author juven
 */
public class LogConfigMessageUtil
{
    private static final Logger LOG = Logger.getLogger( LogConfigMessageUtil.class );

    private static final String SERVICE_URL = "service/local/log/config";

    private XStream xstream;

    private MediaType mediaType;

    public LogConfigMessageUtil( XStream xstream, MediaType mediaType )
    {
        this.xstream = xstream;

        this.mediaType = mediaType;
    }

    public LogConfigResource getLogConfig()
        throws IOException
    {
        String responseText = RequestFacade.doGetRequest( SERVICE_URL ).getEntity().getText();

        LOG.debug( "responseText: \n" + responseText );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseText, mediaType );

        LogConfigResourceResponse resourceResponse = (LogConfigResourceResponse) representation
            .getPayload( new LogConfigResourceResponse() );

        return resourceResponse.getData();

    }

    public void updateLogConfig( LogConfigResource resource )
        throws IOException
    {
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        LogConfigResourceResponse resourceRequest = new LogConfigResourceResponse();

        resourceRequest.setData( resource );

        representation.setPayload( resourceRequest );

        LOG.debug( "requestText: \n" + representation.getText() );

        Response response = RequestFacade.sendMessage( SERVICE_URL, Method.PUT, representation );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not update log config: " + response.getStatus() + "\n" + responseText );
        }
    }
}
