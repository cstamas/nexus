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
package org.sonatype.nexus.rest.global;

import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.email.EmailerException;
import org.sonatype.nexus.email.SmtpSettingsValidator;
import org.sonatype.nexus.rest.model.SmtpSettingsResource;
import org.sonatype.nexus.rest.model.SmtpSettingsResourceRequest;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * The Smtp settings validation resource.
 * 
 * @author velo
 */
@Component( role = PlexusResource.class, hint = "SmtpSettingsValidation" )
@Path( SmtpSettingsValidationPlexusResource.RESOURCE_URI )
@Consumes( { "application/xml", "application/json" } )
public class SmtpSettingsValidationPlexusResource
    extends AbstractGlobalConfigurationPlexusResource
{
    public static final String RESOURCE_URI = "/check_smtp_settings";
        
    private static final Pattern EMAIL_PATTERN = Pattern.compile( ".+@.+\\.[a-z]+" );

    @Requirement
    private SmtpSettingsValidator emailer;

    public SmtpSettingsValidationPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new SmtpSettingsResourceRequest();
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/global_settings/*", "authcBasic,perms[nexus:settings]" );
    }

    /**
     * Validate smtp settings, send a test email using the configuration.
     */
    @Override
    @PUT
    @ResourceMethodSignature( input = SmtpSettingsResourceRequest.class )
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        SmtpSettingsResourceRequest configRequest = (SmtpSettingsResourceRequest) payload;

        SmtpSettingsResource settings = configRequest.getData();

        String email = settings.getTestEmail();

        if ( !EMAIL_PATTERN.matcher( email ).matches() )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid e-mail address: " + email );
        }

        CSmtpConfiguration config = new CSmtpConfiguration();

        config.setHostname( settings.getHost() );

        String oldPassword = getNexusEmailer().getSMTPPassword();

        config.setPassword( this.getActualPassword( settings.getPassword(), oldPassword ) );
        config.setPort( settings.getPort() );
        config.setSslEnabled( settings.isSslEnabled() );
        config.setTlsEnabled( settings.isTlsEnabled() );
        config.setUsername( settings.getUsername() );
        config.setSystemEmailAddress( settings.getSystemEmailAddress().trim() );

        boolean status;
        try
        {
            status = emailer.sendSmtpConfigurationTest( config, email );
        }
        catch ( EmailerException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Failed to send validation e-mail: "
                + e.getMessage(), e );
        }

        if ( status )
        {
            response.setStatus( Status.SUCCESS_OK, "Email was sent. Check your inbox!" );
        }
        else
        {
            response.setStatus( Status.SUCCESS_OK, "Unable to determine if e-mail was sent or not.  Check your inbox!" );
        }

        return null;
    }

}
