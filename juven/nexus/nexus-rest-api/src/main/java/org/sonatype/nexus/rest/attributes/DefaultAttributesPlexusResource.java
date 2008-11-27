package org.sonatype.nexus.rest.attributes;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "DefaultAttributesPlexusResource" )
public class DefaultAttributesPlexusResource
    extends AbstractAttributesPlexusResource
{

    @Override
    public String getResourceUri()
    {
        return "/attributes";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:cache]" );
    }

}
