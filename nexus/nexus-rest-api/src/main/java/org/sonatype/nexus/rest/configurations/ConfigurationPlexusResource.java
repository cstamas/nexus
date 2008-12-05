package org.sonatype.nexus.rest.configurations;

import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.global.GlobalConfigurationPlexusResource;
import org.sonatype.plexus.rest.representation.InputStreamRepresentation;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * A resource that is able to retrieve configurations as stream.
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "configuration" )
public class ConfigurationPlexusResource
    extends AbstractNexusPlexusResource
{
    /** The config key used in URI and request attributes */
    public static final String CONFIG_NAME_KEY = "configName";


    @Override
    public Object getPayloadInstance()
    {
        // this is RO resource, and have no payload, it streams the file to client
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/configs/{" + CONFIG_NAME_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/configs/*", "authcBasic,perms[nexus:configuration]" );
    }

    @Override
    public List<Variant> getVariants()
    {
        List<Variant> result = super.getVariants();

        result.clear();

        result.add( new Variant( MediaType.APPLICATION_XML ) );

        return result;
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String key = request
            .getAttributes().get( GlobalConfigurationPlexusResource.CONFIG_NAME_KEY ).toString();

        try
        {
            NexusStreamResponse result;

            if ( !getNexus().getConfigurationFiles().containsKey( key ) )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "No configuration with key '"
                    + key + "' found!" );
            }
            else
            {
                result = getNexus().getConfigurationAsStreamByKey( key );
            }

            return new InputStreamRepresentation( MediaType.valueOf( result.getMimeType() ), result.getInputStream() );
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "IOException during configuration retrieval!", e );
        }
    }

}
