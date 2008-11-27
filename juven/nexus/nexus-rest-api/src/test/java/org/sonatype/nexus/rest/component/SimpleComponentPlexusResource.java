package org.sonatype.nexus.rest.component;

import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

/**
 * Allows testing of any Role. Actual implementations should only expose a single role
 */
public class SimpleComponentPlexusResource
    extends AbstractComponentListPlexusResource
{

    @Override
    public String getResourceUri()
    {
        return "/components/{" + ROLE_ID + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return null;
    }
}
