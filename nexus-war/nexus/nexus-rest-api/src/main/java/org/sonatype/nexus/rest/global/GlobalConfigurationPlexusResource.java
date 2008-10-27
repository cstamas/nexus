package org.sonatype.nexus.rest.global;

import java.io.IOException;

import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.UsernamePasswordToken;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationResourceResponse;
import org.sonatype.nexus.rest.model.RemoteConnectionSettings;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.nexus.rest.model.SmtpSettings;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

/**
 * The GlobalConfiguration resource. It simply gets and builds the requested config REST model (DTO) and passes
 * serializes it using underlying representation mechanism.
 * 
 * @author cstamas
 * @author tstevens
 * @plexus.component role-hint="GlobalConfigurationPlexusResource"
 */
public class GlobalConfigurationPlexusResource
    extends AbstractGlobalConfigurationPlexusResource
{

    /** The config key used in URI and request attributes */
    public static final String CONFIG_NAME_KEY = "configName";

    /** Name denoting current Nexus configuration */
    public static final String CURRENT_CONFIG_NAME = "current";

    /** Name denoting default Nexus configuration */
    public static final String DEFAULT_CONFIG_NAME = "default";

    public GlobalConfigurationPlexusResource()
    {
        this.setModifiable( true );
    }
    
    @Override
    public Object getPayloadInstance()
    {
        return new GlobalConfigurationResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return "/global_settings/{" + CONFIG_NAME_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/global_settings/*", "authcBasic,perms[nexus:settings]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String configurationName = request.getAttributes().get( CONFIG_NAME_KEY ).toString();

        if ( !DEFAULT_CONFIG_NAME.equals( configurationName ) && !CURRENT_CONFIG_NAME.equals( configurationName ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }
        else
        {
            GlobalConfigurationResource resource = new GlobalConfigurationResource();

            if ( DEFAULT_CONFIG_NAME.equals( configurationName ) )
            {
                fillDefaultConfiguration( resource, this.getNexusInstance( request ) );
            }
            else
            {
                fillCurrentConfiguration( resource, this.getNexusInstance( request ) );
            }

            GlobalConfigurationResourceResponse result = new GlobalConfigurationResourceResponse();

            result.setData( resource );

            return result;
        }
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        String configurationName = request.getAttributes().get( CONFIG_NAME_KEY ).toString();

        if ( !DEFAULT_CONFIG_NAME.equals( configurationName ) && !CURRENT_CONFIG_NAME.equals( configurationName ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }
        else if ( !CURRENT_CONFIG_NAME.equals( configurationName ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
        }
        else
        {
            GlobalConfigurationResourceResponse configRequest = (GlobalConfigurationResourceResponse) payload;

            if ( configRequest != null )
            {
                GlobalConfigurationResource resource = configRequest.getData();

                try
                {
                    if ( resource.getSmtpSettings() != null )
                    {
                        SmtpSettings settings = resource.getSmtpSettings();

                        CSmtpConfiguration config = new CSmtpConfiguration();

                        config.setHost( settings.getHost() );

                        config.setPassword( settings.getPassword() );

                        config.setPort( settings.getPort() );

                        config.setSslEnabled( settings.isSslEnabled() );

                        config.setTlsEnabled( settings.isTlsEnabled() );

                        config.setUsername( settings.getUsername() );

                        config.setSystemEmailAddress( settings.getSystemEmailAddress() );

                        getNexusInstance( request ).updateSmtpConfiguration( config );
                    }

                    if ( resource.getGlobalConnectionSettings() != null )
                    {
                        RemoteConnectionSettings s = resource.getGlobalConnectionSettings();

                        CRemoteConnectionSettings connection = new CRemoteConnectionSettings();

                        connection.setConnectionTimeout( s.getConnectionTimeout() * 1000 );

                        connection.setRetrievalRetryCount( s.getRetrievalRetryCount() );

                        connection.setQueryString( s.getQueryString() );

                        connection.setUserAgentString( s.getUserAgentString() );

                        getNexusInstance( request ).updateGlobalRemoteConnectionSettings( connection );
                    }

                    if ( resource.getGlobalHttpProxySettings() != null
                        && !StringUtils.isEmpty( resource.getGlobalHttpProxySettings().getProxyHostname() ) )
                    {
                        RemoteHttpProxySettings s = resource.getGlobalHttpProxySettings();

                        CRemoteHttpProxySettings proxy = new CRemoteHttpProxySettings();

                        proxy.setProxyHostname( s.getProxyHostname() );

                        proxy.setProxyPort( s.getProxyPort() );

                        if ( s.getAuthentication() != null )
                        {
                            CRemoteAuthentication auth = new CRemoteAuthentication();

                            auth.setUsername( s.getAuthentication().getUsername() );

                            auth.setPassword( s.getAuthentication().getPassword() );

                            auth.setNtlmDomain( s.getAuthentication().getNtlmDomain() );

                            auth.setNtlmHost( s.getAuthentication().getNtlmHost() );

                            auth.setPrivateKey( s.getAuthentication().getPrivateKey() );

                            auth.setPassphrase( s.getAuthentication().getPassphrase() );

                            proxy.setAuthentication( auth );
                        }

                        getNexusInstance( request ).updateGlobalRemoteHttpProxySettings( proxy );
                    }
                    else
                    {
                        getNexusInstance( request ).updateGlobalRemoteHttpProxySettings( null );
                    }

                    getNexusInstance( request ).setSecurityEnabled( resource.isSecurityEnabled() );

                    getNexusInstance( request ).setAnonymousAccessEnabled( resource.isSecurityAnonymousAccessEnabled() );

                    if ( resource.isSecurityAnonymousAccessEnabled()
                        && !StringUtils.isEmpty( resource.getSecurityAnonymousUsername() )
                        && !StringUtils.isEmpty( resource.getSecurityAnonymousPassword() ) )
                    {
                        if ( getNexusInstance( request ).getAnonymousUsername().equals(
                            resource.getSecurityAnonymousUsername() )
                            && !getNexusInstance( request ).getAnonymousPassword().equals(
                                resource.getSecurityAnonymousPassword() ) )
                        {
                            // no user change, only password

                            /*
                             * TODO getNexusSecurityConfiguration().changePassword( getNexusInstance( request
                             * ).getAnonymousUsername(), getNexusInstance( request ).getAnonymousPassword(),
                             * resource.getSecurityAnonymousPassword() );
                             */
                        }
                        else
                        {
                            // user changed too
                            try
                            {
                                // try to "log in" with supplied credentials
                                // the anon user a) should exists b) the pwd must work
                                getSecurityManager( request ).authenticate(
                                    new UsernamePasswordToken( resource.getSecurityAnonymousUsername(), resource
                                        .getSecurityAnonymousPassword() ) );
                            }
                            catch ( AuthenticationException e )
                            {
                                // the supplied anon auth info is wrong
                                getLogger()
                                    .warn(
                                        "Nexus refused to apply configuration, the supplied anonymous information is wrong.",
                                        e );

                                throw new PlexusResourceException(
                                    Status.CLIENT_ERROR_BAD_REQUEST,
                                    e.getMessage(),
                                    getNexusErrorResponse( "securityAnonymousUsername", e.getMessage() ) );
                            }
                        }

                        getNexusInstance( request ).setAnonymousUsername( resource.getSecurityAnonymousUsername() );

                        getNexusInstance( request ).setAnonymousPassword( resource.getSecurityAnonymousPassword() );
                    }
                    else if ( resource.isSecurityAnonymousAccessEnabled() )
                    {
                        // the supplied anon auth info is wrong
                        getLogger()
                            .warn(
                                "Nexus refused to apply configuration, the supplied anonymous username/pwd information is empty." );

                        throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, getNexusErrorResponse(
                            "securityAnonymousUsername",
                            "Cannot be empty when Anonynous access is enabled" ) );
                    }

                    if ( resource.getBaseUrl() != null )
                    {
                        if ( StringUtils.isEmpty( resource.getBaseUrl() ) )
                        {
                            // resetting it
                            getNexusInstance( request ).setBaseUrl( null );
                        }
                        else
                        {
                            // setting it
                            getNexusInstance( request ).setBaseUrl( resource.getBaseUrl() );
                        }
                    }
                }
                catch ( ConfigurationException e )
                {
                    getLogger().warn( "Nexus refused to apply configuration.", e );

                    throw new PlexusResourceException(
                        Status.CLIENT_ERROR_BAD_REQUEST,
                        e.getMessage(),
                        getNexusErrorResponse( "*", e.getMessage() ) );
                }
                catch ( IOException e )
                {
                    getLogger().warn( "Got IO Exception during update of Nexus configuration.", e );

                    throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
                }

            }
        }
        // TODO: this method needs some serious cleaning up...
        response.setStatus( Status.SUCCESS_NO_CONTENT );
        return null;
    }

    /**
     * Externalized Nexus object to DTO's conversion, using default Nexus configuration.
     * 
     * @param resource
     */
    protected void fillDefaultConfiguration( GlobalConfigurationResource resource, Nexus nexus )
    {
        resource.setSecurityEnabled( nexus.isDefaultSecurityEnabled() );

        resource.setSecurityAnonymousAccessEnabled( nexus.isDefaultAnonymousAccessEnabled() );

        resource.setSecurityAnonymousUsername( nexus.getDefaultAnonymousUsername() );

        resource.setSecurityAnonymousPassword( nexus.getDefaultAnonymousPassword() );

        resource.setGlobalConnectionSettings( convert( nexus.readDefaultGlobalRemoteConnectionSettings() ) );

        resource.setGlobalHttpProxySettings( convert( nexus.readDefaultGlobalRemoteHttpProxySettings() ) );

        resource.setSmtpSettings( convert( nexus.readDefaultSmtpConfiguration() ) );
    }

    /**
     * Externalized Nexus object to DTO's conversion, using current Nexus configuration.
     * 
     * @param resource
     */
    protected void fillCurrentConfiguration( GlobalConfigurationResource resource, Nexus nexus )
    {
        resource.setSecurityEnabled( nexus.isSecurityEnabled() );

        resource.setSecurityAnonymousAccessEnabled( nexus.isAnonymousAccessEnabled() );

        resource.setSecurityAnonymousUsername( nexus.getAnonymousUsername() );

        resource.setSecurityAnonymousPassword( nexus.getAnonymousPassword() );

        resource.setGlobalConnectionSettings( convert( nexus.readGlobalRemoteConnectionSettings() ) );

        resource.setGlobalHttpProxySettings( convert( nexus.readGlobalRemoteHttpProxySettings() ) );

        resource.setBaseUrl( nexus.getBaseUrl() );

        resource.setSmtpSettings( convert( nexus.readSmtpConfiguration() ) );
    }

    protected String getSecurityConfiguration( boolean enabled, String authSourceType )
    {
        if ( !enabled )
        {
            return SECURITY_OFF;
        }
        else
        {
            if ( SECURITY_SIMPLE.equals( authSourceType ) )
            {
                return SECURITY_SIMPLE;
            }
            else
            {
                return SECURITY_CUSTOM;
            }
        }
    }

}
