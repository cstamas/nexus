package org.sonatype.nexus.rest.repotargets;

import java.io.IOException;
import java.util.Collection;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.nexus.rest.model.RepositoryTargetListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RepositoryTargetListPlexusResource" )
public class RepositoryTargetListPlexusResource
    extends AbstractRepositoryTargetPlexusResource
{

    public RepositoryTargetListPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryTargetResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return "/repo_targets";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:targets]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryTargetListResourceResponse result = new RepositoryTargetListResourceResponse();

        Collection<CRepositoryTarget> targets = getNexus().listRepositoryTargets();

        RepositoryTargetListResource res = null;

        for ( CRepositoryTarget target : targets )
        {
            res = new RepositoryTargetListResource();

            res.setId( target.getId() );

            res.setName( target.getName() );

            res.setContentClass( target.getContentClass() );

            res.setResourceURI( this.createChildReference( request, target.getId() ).toString() );

            result.addData( res );
        }

        return result;
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryTargetResourceResponse result = (RepositoryTargetResourceResponse) payload;
        RepositoryTargetResourceResponse resourceResponse = null;

        if ( result != null )
        {
            RepositoryTargetResource resource = result.getData();

            if ( validate( true, resource ) )
            {
                try
                {
                    CRepositoryTarget target = getRestToNexusResource( resource );

                    // create
                    getNexus().createRepositoryTarget( target );

                    // response
                    resourceResponse = new RepositoryTargetResourceResponse();

                    resourceResponse.setData( result.getData() );
                }
                catch ( ConfigurationException e )
                {
                    // build an exception and throws it
                    handleConfigurationException( e );
                }
                catch ( IOException e )
                {
                    getLogger().warn( "Got IOException during creation of repository target!", e );

                    throw new ResourceException(
                        Status.SERVER_ERROR_INTERNAL,
                        "Got IOException during creation of repository target!" );
                }
            }
        }
        return resourceResponse;
    }
}
