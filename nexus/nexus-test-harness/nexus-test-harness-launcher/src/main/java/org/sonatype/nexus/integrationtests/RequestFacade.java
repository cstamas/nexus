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
package org.sonatype.nexus.integrationtests;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.log4j.Logger;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.codehaus.plexus.util.IOUtil;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

public class RequestFacade
{
    public static final String SERVICE_LOCAL = "service/local/";

    private static final Logger LOG = Logger.getLogger( RequestFacade.class );

    public static Response doGetRequest( String serviceURIpart )
        throws IOException
    {
        return sendMessage( serviceURIpart, Method.GET );
    }

    public static Response sendMessage( String serviceURIpart, Method method )
        throws IOException
    {
        return sendMessage( serviceURIpart, method, null );
    }

    public static Response sendMessage( String serviceURIpart, Method method, Representation representation )
        throws IOException
    {
        String serviceURI = AbstractNexusIntegrationTest.nexusBaseUrl + serviceURIpart;
        return sendMessage( new URL( serviceURI ), method, representation );
    }

    public static Response sendMessage( URL url, Method method, Representation representation )
        throws IOException
    {

        Request request = new Request();
        request.setResourceRef( url.toString() );
        request.setMethod( method );

        if ( !Method.GET.equals( method ) && !Method.DELETE.equals( method ) )
        {
            request.setEntity( representation );
        }

        // change the MediaType if this is a GET, default to application/xml
        if( Method.GET.equals( method ) )
        {
            if( representation != null)
            {
                request.getClientInfo().getAcceptedMediaTypes().
                add(new Preference<MediaType>(representation.getMediaType()));
            }
        }

        // check the text context to see if this is a secure test
        TestContext context = TestContainer.getInstance().getTestContext();
        if ( context.isSecureTest() )
        {
            // ChallengeScheme scheme = new ChallengeScheme( "HTTP_NxBasic", "NxBasic", "Nexus Basic" );
            ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC,
                context.getUsername(),
                context.getPassword() );
            request.setChallengeResponse( authentication );
        }

        Context ctx = new Context();

        Client client = new Client( ctx, Protocol.HTTP );

        LOG.debug( "sendMessage: " + method.getName() + " " + url );
        return client.handle( request );
    }

    public static File downloadFile( URL url, String targetFile )
        throws IOException
    {
        OutputStream out = null;
        InputStream in = null;
        File downloadedFile = new File( targetFile );

        try
        {
            Response response = sendMessage( url, Method.GET, null );

            if ( !response.getStatus().isSuccess() )
            {
                throw new FileNotFoundException( response.getStatus() + " - " + url );
            }

            // if this is null then someone was getting really creative with the tests, but hey, we will let them...
            if ( downloadedFile.getParentFile() != null )
            {
                downloadedFile.getParentFile().mkdirs();
            }

            in = response.getEntity().getStream();
            out = new BufferedOutputStream( new FileOutputStream( downloadedFile ) );

            IOUtil.copy( in, out, 1024 );
        }
        finally
        {
            IOUtil.close( in );
            IOUtil.close( out );
        }
        return downloadedFile;
    }

    public static HttpMethod executeHTTPClientMethod( URL url, HttpMethod method )
        throws HttpException,
            IOException
    {

        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout( 5000 );

        // check the text context to see if this is a secure test
        TestContext context = TestContainer.getInstance().getTestContext();
        if ( context.isSecureTest() )
        {
            client.getState().setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials( context.getUsername(), context.getPassword() ) );

            List<String> authPrefs = new ArrayList<String>( 1 );
            authPrefs.add( AuthPolicy.BASIC );
            client.getParams().setParameter( AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs );
            client.getParams().setAuthenticationPreemptive( true );
        }
        try
        {
            client.executeMethod( method );
            method.getResponseBodyAsString();
            return method;
        }
        finally
        {
            method.releaseConnection();
        }
    }

    public static AuthenticationInfo getWagonAuthenticationInfo()
    {
        AuthenticationInfo authInfo = null;
        // check the text context to see if this is a secure test
        TestContext context = TestContainer.getInstance().getTestContext();
        if ( context.isSecureTest() )
        {
            authInfo = new AuthenticationInfo();
            authInfo.setUserName( context.getUsername() );
            authInfo.setPassword( context.getPassword() );
        }
        return authInfo;
    }

}
