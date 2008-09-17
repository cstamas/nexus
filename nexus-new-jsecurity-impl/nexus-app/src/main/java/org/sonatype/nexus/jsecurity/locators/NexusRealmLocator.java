package org.sonatype.nexus.jsecurity.locators;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.ServiceLocator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Serviceable;
import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.locators.RealmLocator;
import org.sonatype.nexus.configuration.application.NexusConfiguration;

/**
 * The nexus implementation of the realm locator
 * will load from the nexus.xml file
 * 
 * @plexus.component role="org.sonatype.jsecurity.locators.RealmLocator"
 */
public class NexusRealmLocator
    extends AbstractLogEnabled
    implements
    RealmLocator, Serviceable
{    
    /**
     * @plexus.requirement
     */
    NexusConfiguration configuration;
    
    private ServiceLocator container;
    
    public void service( ServiceLocator locator )
    {
        this.container = locator;
    }
    
    public List<Realm> getRealms()
    {
        // Until nexus.xml code is done, simply returning hardcoded realms
        
        List<Realm> realms = new ArrayList<Realm>();
        
        try
        {
            realms.add( ( Realm ) container.lookup( "org.jsecurity.realm.Realm", "NexusTargetRealm" ) );
        }
        catch ( ComponentLookupException e )
        {
            getLogger().error( "Unable to lookup security realms", e );
        }
        
        return realms;
    }

}
