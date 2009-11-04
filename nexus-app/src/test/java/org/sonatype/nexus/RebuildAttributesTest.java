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
package org.sonatype.nexus;

import java.io.IOException;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven2HostedRepositoryTemplate;

public class RebuildAttributesTest
    extends AbstractNexusTestCase
{
    private DefaultNexus defaultNexus;

    private RepositoryRegistry repositoryRegistry;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        defaultNexus = (DefaultNexus) lookup( Nexus.class );

        repositoryRegistry = lookup( RepositoryRegistry.class );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    public DefaultNexus getDefaultNexus()
    {
        return defaultNexus;
    }

    public void setDefaultNexus( DefaultNexus defaultNexus )
    {
        this.defaultNexus = defaultNexus;
    }

    public void testRepositoryRebuildAttributes()
        throws IOException
    {
        try
        {
            RepositoryTemplate hostedRepoTemplate =
                (RepositoryTemplate) getDefaultNexus().getRepositoryTemplates()
                    .getTemplates( Maven2HostedRepositoryTemplate.class ).getTemplates( RepositoryPolicy.RELEASE )
                    .pick();

            hostedRepoTemplate.getConfigurableRepository().setId( "test" );
            hostedRepoTemplate.getConfigurableRepository().setName( "Test" );
            hostedRepoTemplate.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );

            hostedRepoTemplate.create();

            repositoryRegistry.getRepository( "test" )
                .recreateAttributes( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT ), null );
        }
        catch ( ConfigurationException e )
        {
            fail( "ConfigurationException creating repository" );
        }
        catch ( NoSuchRepositoryException e )
        {
            fail( "NoSuchRepositoryException reindexing repository" );
        }
    }
}
