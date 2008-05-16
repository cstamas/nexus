/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.nexus.artifact.NexusItemInfo;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteNexusInstance;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.http.HttpProxyService;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.registry.InvalidGroupingException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryType;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.schedule.Scheduler;
import org.sonatype.nexus.store.DefaultEntry;
import org.sonatype.nexus.store.Entry;
import org.sonatype.nexus.store.Store;

/**
 * The default Nexus implementation.
 * 
 * @author Jason van Zyl
 * @author cstamas
 * @plexus.component
 */
public class DefaultNexus
    extends AbstractLogEnabled
    implements Nexus, Initializable, Startable, EventListener, ConfigurationChangeListener
{
    /**
     * The nexus configuration.
     * 
     * @plexus.requirement
     */
    private NexusConfiguration nexusConfiguration;

    /**
     * The NexusIndexer.
     * 
     * @plexus.requirement
     */
    private IndexerManager indexerManager;

    /**
     * The repository registry.
     * 
     * @plexus.requirement
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * The store for templates.
     * 
     * @plexus.requirement role-hint="file"
     */
    private Store templatesStore;

    /**
     * The http proxy device.
     * 
     * @plexus.requirement
     */
    private HttpProxyService httpProxyService;

    /**
     * The Scheduler.
     * 
     * @plexus.requirement
     */
    private Scheduler scheduler;

    /**
     * The Feed recorder.
     * 
     * @plexus.requirement
     */
    private FeedRecorder feedRecorder;

    /**
     * System status.
     */
    private SystemStatus systemStatus;

    // ----------------------------------------------------------------------------------------------------------
    // Template names and prefixes, not allowed to go out of this class
    // ----------------------------------------------------------------------------------------------------------

    private static final String TEMPLATE_REPOSITORY_PREFIX = "repository-";

    private static final String TEMPLATE_REPOSITORY_SHADOW_PREFIX = "repositoryShadow-";

    private static final String TEMPLATE_DEFAULT_PROXY_RELEASE = "default_proxy_release";

    private static final String TEMPLATE_DEFAULT_PROXY_SNAPSHOT = "default_proxy_snapshot";

    private static final String TEMPLATE_DEFAULT_HOSTED_RELEASE = "default_hosted_release";

    private static final String TEMPLATE_DEFAULT_HOSTED_SNAPSHOT = "default_hosted_snapshot";

    private static final String TEMPLATE_DEFAULT_VIRTUAL = "default_virtual";

    // ----------------------------------------------------------------------------------------------------------
    // SystemStatus
    // ----------------------------------------------------------------------------------------------------------

    public SystemStatus getSystemState()
    {
        return systemStatus;
    }

    // ----------------------------------------------------------------------------------------------------------
    // Repositories
    // ----------------------------------------------------------------------------------------------------------

    public Repository getRepository( String repoId )
        throws NoSuchRepositoryException
    {
        return repositoryRegistry.getRepository( repoId );
    }

    public Collection<Repository> getRepositories()
    {
        return repositoryRegistry.getRepositories();
    }

    public StorageItem dereferenceLinkItem( StorageItem item )
        throws NoSuchRepositoryException,
            ItemNotFoundException,
            AccessDeniedException,
            RepositoryNotAvailableException,
            StorageException

    {
        if ( StorageLinkItem.class.isAssignableFrom( item.getClass() ) )
        {
            // it is a link
            RepositoryItemUid uid = new RepositoryItemUid( repositoryRegistry, ( (StorageLinkItem) item ).getTarget() );

            return uid.getRepository().retrieveItem( false, uid );
        }
        else
        {
            return item;
        }
    }

    // ------------------------------------------------------------------
    // CRUD-like ops on config sections

    public String getAuthenticationSourceType()
    {
        return nexusConfiguration.getAuthenticationSourceType();
    }

    public boolean isAnonymousAccessEnabled()
    {
        return nexusConfiguration.isAnonymousAccessEnabled();
    }

    public boolean isSecurityEnabled()
    {
        return nexusConfiguration.isSecurityEnabled();
    }
    
    public boolean isSimpleSecurityModel()
    {
        return nexusConfiguration.isSimpleSecurityModel();
    }

    public void setSecurity( boolean enabled, String authenticationSourceType )
        throws IOException
    {
        nexusConfiguration.setSecurity( enabled, authenticationSourceType );
    }

    public String getBaseUrl()
    {
        return nexusConfiguration.getBaseUrl();
    }

    public void setBaseUrl( String baseUrl )
        throws IOException
    {
        nexusConfiguration.setBaseUrl( baseUrl );
    }

    // Globals are mandatory: RU

    public String readWorkingDirectory()
    {
        return nexusConfiguration.readWorkingDirectory();
    }

    public void updateWorkingDirectory( String settings )
        throws IOException
    {
        nexusConfiguration.updateWorkingDirectory( settings );
    }

    public String readApplicationLogDirectory()
    {
        return nexusConfiguration.readApplicationLogDirectory();
    }

    public void updateApplicationLogDirectory( String settings )
        throws IOException
    {
        nexusConfiguration.updateApplicationLogDirectory( settings );
    }

    // CRemoteConnectionSettings are mandatory: RU

    public CRemoteConnectionSettings readGlobalRemoteConnectionSettings()
    {
        return nexusConfiguration.readGlobalRemoteConnectionSettings();
    }

    public void updateGlobalRemoteConnectionSettings( CRemoteConnectionSettings settings )
        throws ConfigurationException,
            IOException
    {
        nexusConfiguration.updateGlobalRemoteConnectionSettings( settings );
    }

    // CRemoteHttpProxySettings are optional: CRUD

    public void createGlobalRemoteHttpProxySettings( CRemoteHttpProxySettings settings )
        throws ConfigurationException,
            IOException
    {
        nexusConfiguration.createGlobalRemoteHttpProxySettings( settings );
    }

    public CRemoteHttpProxySettings readGlobalRemoteHttpProxySettings()
    {
        return nexusConfiguration.readGlobalRemoteHttpProxySettings();
    }

    public void updateGlobalRemoteHttpProxySettings( CRemoteHttpProxySettings settings )
        throws ConfigurationException,
            IOException
    {
        nexusConfiguration.updateGlobalRemoteHttpProxySettings( settings );
    }

    public void deleteGlobalRemoteHttpProxySettings()
        throws IOException
    {
        nexusConfiguration.deleteGlobalRemoteHttpProxySettings();
    }

    // CRouting are mandatory: RU

    public CRouting readRouting()
    {
        return nexusConfiguration.readRouting();
    }

    public void updateRouting( CRouting settings )
        throws ConfigurationException,
            IOException
    {
        nexusConfiguration.updateRouting( settings );
    }

    // CRepository: CRUD

    public Collection<CRepository> listRepositories()
    {
        return nexusConfiguration.listRepositories();
    }

    public void createRepository( CRepository settings )
        throws ConfigurationException,
            IOException
    {
        nexusConfiguration.createRepository( settings );
    }

    public CRepository readRepository( String id )
        throws NoSuchRepositoryException
    {
        return nexusConfiguration.readRepository( id );
    }

    public void updateRepository( CRepository settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        nexusConfiguration.updateRepository( settings );
    }

    public void deleteRepository( String id )
        throws NoSuchRepositoryException,
            IOException,
            ConfigurationException
    {
        nexusConfiguration.deleteRepository( id );
    }

    // CRepositoryShadow: CRUD

    public Collection<CRepositoryShadow> listRepositoryShadows()
    {
        return nexusConfiguration.listRepositoryShadows();
    }

    public void createRepositoryShadow( CRepositoryShadow settings )
        throws ConfigurationException,
            IOException
    {
        nexusConfiguration.createRepositoryShadow( settings );
    }

    public CRepositoryShadow readRepositoryShadow( String id )
        throws NoSuchRepositoryException
    {
        return nexusConfiguration.readRepositoryShadow( id );
    }

    public void updateRepositoryShadow( CRepositoryShadow settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        nexusConfiguration.updateRepositoryShadow( settings );
    }

    public void deleteRepositoryShadow( String id )
        throws NoSuchRepositoryException,

            IOException
    {
        nexusConfiguration.deleteRepositoryShadow( id );
    }

    // CGroupsSettingPathMapping: CRUD

    public Collection<CGroupsSettingPathMappingItem> listGroupsSettingPathMapping()
    {
        return nexusConfiguration.listGroupsSettingPathMapping();
    }

    public void createGroupsSettingPathMapping( CGroupsSettingPathMappingItem settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        nexusConfiguration.createGroupsSettingPathMapping( settings );
    }

    public CGroupsSettingPathMappingItem readGroupsSettingPathMapping( String id )
        throws IOException
    {
        return nexusConfiguration.readGroupsSettingPathMapping( id );
    }

    public void updateGroupsSettingPathMapping( CGroupsSettingPathMappingItem settings )
        throws NoSuchRepositoryException,
            ConfigurationException,
            IOException
    {
        nexusConfiguration.updateGroupsSettingPathMapping( settings );
    }

    public void deleteGroupsSettingPathMapping( String id )
        throws IOException
    {
        nexusConfiguration.deleteGroupsSettingPathMapping( id );
    }

    // CRepositoryGroup: CRUD

    public Collection<CRepositoryGroup> listRepositoryGroups()
    {
        return nexusConfiguration.listRepositoryGroups();
    }

    public void createRepositoryGroup( CRepositoryGroup settings )
        throws NoSuchRepositoryException,
            InvalidGroupingException,
            IOException
    {
        nexusConfiguration.createRepositoryGroup( settings );
    }

    public CRepositoryGroup readRepositoryGroup( String id )
        throws NoSuchRepositoryGroupException
    {
        return nexusConfiguration.readRepositoryGroup( id );
    }

    public void updateRepositoryGroup( CRepositoryGroup settings )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            InvalidGroupingException,
            IOException
    {
        nexusConfiguration.updateRepositoryGroup( settings );
    }

    public void deleteRepositoryGroup( String id )
        throws NoSuchRepositoryGroupException,
            IOException
    {
        nexusConfiguration.deleteRepositoryGroup( id );
    }

    public Collection<CRemoteNexusInstance> listRemoteNexusInstances()
    {
        return nexusConfiguration.listRemoteNexusInstances();
    }

    public CRemoteNexusInstance readRemoteNexusInstance( String alias )
        throws IOException
    {
        return nexusConfiguration.readRemoteNexusInstance( alias );
    }

    public void createRemoteNexusInstance( CRemoteNexusInstance settings )
        throws IOException
    {
        nexusConfiguration.createRemoteNexusInstance( settings );
    }

    public void deleteRemoteNexusInstance( String alias )
        throws IOException
    {
        nexusConfiguration.deleteRemoteNexusInstance( alias );
    }

    // =============
    // Maintenance

    public InputStream getConfigurationAsStream()
        throws IOException
    {
        return nexusConfiguration.getConfigurationAsStream();
    }

    public Collection<String> getApplicationLogFiles()
        throws IOException
    {
        getLogger().info( "List log files." );
        File logDir = nexusConfiguration.getApplicationLogDirectory();
        File[] dir = logDir.listFiles();
        if ( dir != null )
        {
            ArrayList<String> result = new ArrayList<String>( dir.length );
            for ( int i = 0; i < dir.length; i++ )
            {
                result.add( dir[i].getName() );
            }
            return result;
        }
        else
        {
            throw new IOException(
                "The listing of \"applicationLogDirectory\" was not possible. Does it points to an existing directory?" );
        }
    }

    /**
     * Retrieves a stream to the requested log file. This method ensures that the file is rooted in the log folder to
     * prevent browsing of the file system.
     * 
     * @param logFile path of the file to retrieve
     * @returns InputStream to the file or null if the file is not allowed or doesn't exist.
     */
    public InputStream getApplicationLogAsStream( String logFile )
        throws IOException
    {
        if ( !logFile.contains( File.pathSeparator ) )
        {
            getLogger().info( "Retrieving " + logFile + " log file." );

            File log = new File( nexusConfiguration.getApplicationLogDirectory(), logFile );

            // "chroot"ing it to nexus log dir
            if ( log.exists()
                && log.getAbsolutePath().startsWith( nexusConfiguration.getApplicationLogDirectory().getAbsolutePath() ) )
            {
                return new FileInputStream( log );
            }
        }

        return null;
    }

    public void clearCaches( String path, String repositoryId, String repositoryGroupId )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException
    {
        if ( repositoryId != null )
        {
            getLogger().info( "Clearing caches in repository " + repositoryId + " from path " + path );

            repositoryRegistry.getRepository( repositoryId ).clearCaches( path );
        }
        else if ( repositoryGroupId != null )
        {
            getLogger().info( "Clearing caches in repository group " + repositoryGroupId + " from path " + path );

            for ( Repository repository : repositoryRegistry.getRepositoryGroup( repositoryGroupId ) )
            {
                repository.clearCaches( path );
            }
        }
        else
        {
            getLogger().info( "Clearing caches in all repositories " + repositoryGroupId + " from path " + path );

            for ( Repository repository : repositoryRegistry.getRepositories() )
            {
                repository.clearCaches( path );
            }
        }
    }

    // ------------------------------------------------------------------
    // Repo templates, CRUD

    protected Collection<Entry> filterOnPrefix( Collection<Entry> entries, String prefix )
    {
        if ( prefix == null )
        {
            return entries;
        }

        Collection<Entry> result = new ArrayList<Entry>();

        for ( Entry e : entries )
        {
            if ( e.getId().startsWith( prefix ) )
            {
                result.add( e );
            }
        }

        return result;
    }

    public Collection<CRepository> listRepositoryTemplates()
        throws IOException
    {
        Collection<Entry> entries = filterOnPrefix( templatesStore.getEntries(), TEMPLATE_REPOSITORY_PREFIX );

        ArrayList<CRepository> result = new ArrayList<CRepository>( entries.size() );

        for ( Entry entry : entries )
        {
            result.add( (CRepository) entry.getContent() );
        }

        return result;
    }

    public void createRepositoryTemplate( CRepository settings )
        throws IOException
    {
        createRepositoryTemplate( settings, true );
    }

    public void createRepositoryTemplate( CRepository settings, boolean replace )
        throws IOException
    {
        DefaultEntry entry = new DefaultEntry( TEMPLATE_REPOSITORY_PREFIX + settings.getId(), settings );

        if ( replace || templatesStore.getEntry( entry.getId() ) == null )
        {
            templatesStore.addEntry( entry );
        }
    }

    public CRepository readRepositoryTemplate( String id )
        throws IOException
    {
        Entry entry = templatesStore.getEntry( TEMPLATE_REPOSITORY_PREFIX + id );

        if ( entry != null )
        {
            return (CRepository) entry.getContent();
        }
        else
        {
            // check for default
            if ( TEMPLATE_DEFAULT_HOSTED_RELEASE.equals( id ) || TEMPLATE_DEFAULT_HOSTED_SNAPSHOT.equals( id )
                || TEMPLATE_DEFAULT_PROXY_RELEASE.equals( id ) || TEMPLATE_DEFAULT_PROXY_SNAPSHOT.equals( id ) )
            {
                createDefaultTemplate( id, false );

                return readRepositoryTemplate( id );
            }

            return null;
        }
    }

    public void updateRepositoryTemplate( CRepository settings )
        throws IOException
    {
        deleteRepositoryTemplate( settings.getId() );

        createRepositoryTemplate( settings );
    }

    public void deleteRepositoryTemplate( String id )
        throws IOException
    {
        templatesStore.removeEntry( TEMPLATE_REPOSITORY_PREFIX + id );
    }

    public Collection<CRepositoryShadow> listRepositoryShadowTemplates()
        throws IOException
    {
        Collection<Entry> entries = filterOnPrefix( templatesStore.getEntries(), TEMPLATE_REPOSITORY_SHADOW_PREFIX );

        ArrayList<CRepositoryShadow> result = new ArrayList<CRepositoryShadow>( entries.size() );

        for ( Entry entry : entries )
        {
            result.add( (CRepositoryShadow) entry.getContent() );
        }

        return result;
    }

    public void createRepositoryShadowTemplate( CRepositoryShadow settings )
        throws IOException
    {
        createRepositoryShadowTemplate( settings, true );
    }

    public void createRepositoryShadowTemplate( CRepositoryShadow settings, boolean replace )
        throws IOException
    {
        DefaultEntry entry = new DefaultEntry( TEMPLATE_REPOSITORY_SHADOW_PREFIX + settings.getId(), settings );

        if ( replace || templatesStore.getEntry( entry.getId() ) == null )
        {
            templatesStore.addEntry( entry );
        }
    }

    public CRepositoryShadow readRepositoryShadowTemplate( String id )
        throws IOException
    {
        Entry entry = templatesStore.getEntry( TEMPLATE_REPOSITORY_SHADOW_PREFIX + id );

        if ( entry != null )
        {
            return (CRepositoryShadow) entry.getContent();
        }
        else
        {
            // check for default
            if ( TEMPLATE_DEFAULT_VIRTUAL.equals( id ) )
            {
                createDefaultTemplate( id, false );

                return readRepositoryShadowTemplate( id );
            }
            return null;
        }
    }

    public void updateRepositoryShadowTemplate( CRepositoryShadow settings )
        throws IOException
    {
        deleteRepositoryShadowTemplate( settings.getId() );

        createRepositoryShadowTemplate( settings );
    }

    public void deleteRepositoryShadowTemplate( String id )
        throws IOException
    {
        templatesStore.removeEntry( TEMPLATE_REPOSITORY_SHADOW_PREFIX + id );
    }

    // ------------------------------------------------------------------
    // Configuration defaults

    public String getDefaultAuthenticationSourceType()
    {
        if ( nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfiguration().getSecurity().getAuthenticationSource() != null )
        {
            return nexusConfiguration
                .getConfigurationSource().getDefaultsSource().getConfiguration().getSecurity()
                .getAuthenticationSource().getType();
        }
        else
        {
            return null;
        }
    }

    public boolean isDefaultAnonymousAccessEnabled()
    {
        return nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfiguration().getSecurity().isAnonymousAccessEnabled();
    }

    public boolean isDefaultSecurityEnabled()
    {
        return nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfiguration().getSecurity().isEnabled();
    }

    public InputStream getDefaultConfigurationAsStream()
        throws IOException
    {
        return nexusConfiguration.getConfigurationSource().getDefaultsSource().getConfigurationAsStream();
    }

    public String readDefaultWorkingDirectory()
    {
        return nexusConfiguration.getConfigurationSource().getDefaultsSource().getConfiguration().getWorkingDirectory();
    }

    public String readDefaultApplicationLogDirectory()
    {
        return nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfiguration().getApplicationLogDirectory();
    }

    public CRemoteConnectionSettings readDefaultGlobalRemoteConnectionSettings()
    {
        return nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfiguration().getGlobalConnectionSettings();
    }

    public CRemoteHttpProxySettings readDefaultGlobalRemoteHttpProxySettings()
    {
        return nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfiguration().getGlobalHttpProxySettings();
    }

    public CRouting readDefaultRouting()
    {
        return nexusConfiguration.getConfigurationSource().getDefaultsSource().getConfiguration().getRouting();
    }

    // =============
    // Feeds

    public FeedRecorder getFeedRecorder()
    {
        return feedRecorder;
    }

    // =============
    // Search and indexing related

    public void reindexAllRepositories()
        throws IOException
    {
        indexerManager.reindexAllRepositories();
    }

    public void reindexRepository( String repositoryId )
        throws NoSuchRepositoryException,
            IOException
    {
        indexerManager.reindexRepository( repositoryId );
    }

    public void reindexRepositoryGroup( String repositoryGroupId )
        throws NoSuchRepositoryGroupException,
            IOException
    {
        indexerManager.reindexRepositoryGroup( repositoryGroupId );
    }

    public void rebuildAttributesAllRepositories()
        throws IOException
    {
        List<Repository> reposes = repositoryRegistry.getRepositories();

        for ( Repository repo : reposes )
        {
            repo.recreateAttributes( null );
        }
    }

    public void rebuildAttributesRepository( String repositoryId )
        throws NoSuchRepositoryException,
            IOException
    {
        repositoryRegistry.getRepository( repositoryId ).recreateAttributes( null );
    }

    public void rebuildAttributesRepositoryGroup( String repositoryGroupId )
        throws NoSuchRepositoryGroupException,
            IOException
    {
        List<Repository> reposes = repositoryRegistry.getRepositoryGroup( repositoryGroupId );

        for ( Repository repo : reposes )
        {
            repo.recreateAttributes( null );
        }
    }

    public ArtifactInfo identifyArtifact( String type, String checksum )
        throws IOException
    {
        try
        {
            return indexerManager.identifyArtifact( type, checksum );
        }
        catch ( IndexContextInInconsistentStateException e )
        {
            getLogger().error( "Index is corrupt.", e );
            return null;
        }
    }

    public Collection<ArtifactInfo> searchArtifactFlat( String term, String repositoryId, String groupId )
    {
        return indexerManager.searchArtifactFlat( term, repositoryId, groupId );
    }

    public Collection<ArtifactInfo> searchArtifactFlat( String gTerm, String aTerm, String vTerm, String cTerm,
        String repositoryId, String groupId )
    {
        return indexerManager.searchArtifactFlat( gTerm, aTerm, vTerm, cTerm, repositoryId, groupId );
    }

    // ===========================
    // Nexus Application lifecycle

    public void initialize()
        throws InitializationException
    {
        systemStatus = new SystemStatus();

        repositoryRegistry.addProximityEventListener( this );

        repositoryRegistry.addProximityEventListener( indexerManager );

        systemStatus.setState( SystemState.STOPPED );

        systemStatus.setOperationMode( OperationMode.STANDALONE );

        systemStatus.setInitializedAt( new Date() );

        try
        {
            Properties props = new Properties();

            InputStream is = getClass().getResourceAsStream(
                "/META-INF/maven/org.sonatype.nexus/nexus-app/pom.properties" );

            if ( is != null )
            {
                props.load( is );
            }

            systemStatus.setVersion( props.getProperty( "version" ) );
        }
        catch ( IOException e )
        {
            getLogger().warn(
                "Could not load/read Nexus version from /META-INF/maven/org.sonatype.nexus/nexus-app/pom.properties",
                e );

            systemStatus.setVersion( "unknown" );
        }

        getLogger().info( "Initialized Nexus (version " + systemStatus.getVersion() + ")" );
    }

    public void start()
        throws StartingException
    {
        systemStatus.setState( SystemState.STARTING );

        try
        {
            // force config load and validation
            // applies configuration and notifies listeners
            nexusConfiguration.loadConfiguration( true );

            feedRecorder.startService();

            getFeedRecorder().addSystemEvent(
                FeedRecorder.SYSTEM_BOOT_ACTION,
                "Starting Nexus (version " + systemStatus.getVersion() + ")" );

            systemStatus.setLastConfigChange( new Date() );

            systemStatus.setConfigurationValidationResponse( nexusConfiguration
                .getConfigurationSource().getValidationResponse() );

            systemStatus.setFirstStart( nexusConfiguration.isConfigurationDefaulted() );

            systemStatus.setInstanceUpgraded( nexusConfiguration.isInstanceUpgradeNeeded() );

            // creating default templates if needed
            createDefaultTemplate( TEMPLATE_DEFAULT_HOSTED_RELEASE, systemStatus.isInstanceUpgraded() );

            createDefaultTemplate( TEMPLATE_DEFAULT_HOSTED_SNAPSHOT, systemStatus.isInstanceUpgraded() );

            createDefaultTemplate( TEMPLATE_DEFAULT_PROXY_RELEASE, systemStatus.isInstanceUpgraded() );

            createDefaultTemplate( TEMPLATE_DEFAULT_PROXY_SNAPSHOT, systemStatus.isInstanceUpgraded() );

            createDefaultTemplate( TEMPLATE_DEFAULT_VIRTUAL, systemStatus.isInstanceUpgraded() );

            if ( systemStatus.isFirstStart() )
            {
                getLogger().info( "This is 1st start of new Nexus instance." );

                // TODO: a virgin instance, inital config created
            }

            if ( systemStatus.isInstanceUpgraded() )
            {
                getLogger().info( "This is an upgraded instance of Nexus." );

                // TODO: perform upgrade or something
            }

            scheduler.submit( new Callable<Object>()
            {
                public Object call()
                    throws Exception
                {
                    indexerManager.publishAllIndex();

                    return null;
                }
            } );

            systemStatus.setState( SystemState.STARTED );

            systemStatus.setStartedAt( new Date() );
        }
        catch ( IOException e )
        {
            systemStatus.setState( SystemState.BROKEN_IO );

            systemStatus.setConfigurationValidationResponse( nexusConfiguration
                .getConfigurationSource().getValidationResponse() );

            systemStatus.setErrorCause( e );

            getLogger().error( "Could not start Nexus, bad IO exception!", e );

            throw new StartingException( "Could not start Nexus!", e );
        }
        catch ( ConfigurationException e )
        {
            systemStatus.setState( SystemState.BROKEN_CONFIGURATION );

            systemStatus.setConfigurationValidationResponse( nexusConfiguration
                .getConfigurationSource().getValidationResponse() );

            systemStatus.setErrorCause( e );

            getLogger().error( "Could not start Nexus, user configuration exception!", e );

            throw new StartingException( "Could not start Nexus!", e );
        }

        getLogger().info( "Started Nexus (version " + systemStatus.getVersion() + ")" );
    }

    public void stop()
        throws StoppingException
    {
        systemStatus.setState( SystemState.STOPPING );

        getFeedRecorder().addSystemEvent(
            FeedRecorder.SYSTEM_BOOT_ACTION,
            "Stopping Nexus (version " + systemStatus.getVersion() + ")" );

        httpProxyService.stopService();

        try
        {
            feedRecorder.stopService();
        }
        catch ( IOException e )
        {
            getLogger().error( "Error while stopping FeedRecorder:", e );
        }

        systemStatus.setState( SystemState.STOPPED );

        getLogger().info( "Stopped Nexus (version " + systemStatus.getVersion() + ")" );
    }

    private void createDefaultTemplate( String id, boolean shouldRecreate )
        throws IOException
    {
        if ( TEMPLATE_DEFAULT_HOSTED_RELEASE.equals( id ) )
        {
            getLogger().info( "Creating default hosted release repository template..." );

            CRepository hostedTemplate = new CRepository();

            hostedTemplate.setId( TEMPLATE_DEFAULT_HOSTED_RELEASE );

            hostedTemplate.setName( "" );

            hostedTemplate.setRepositoryPolicy( CRepository.REPOSITORY_POLICY_RELEASE );

            hostedTemplate.setArtifactMaxAge( -1 );

            hostedTemplate.setMetadataMaxAge( 1440 );

            hostedTemplate.setAllowWrite( true );

            hostedTemplate.setDownloadRemoteIndexes( false );

            createRepositoryTemplate( hostedTemplate, shouldRecreate );
        }
        else if ( TEMPLATE_DEFAULT_HOSTED_SNAPSHOT.equals( id ) )
        {
            getLogger().info( "Creating default hosted snapshot repository template..." );

            CRepository hostedTemplate = new CRepository();

            hostedTemplate.setId( TEMPLATE_DEFAULT_HOSTED_SNAPSHOT );

            hostedTemplate.setName( "" );

            hostedTemplate.setRepositoryPolicy( CRepository.REPOSITORY_POLICY_SNAPSHOT );

            hostedTemplate.setArtifactMaxAge( 1440 );

            hostedTemplate.setMetadataMaxAge( 1440 );

            hostedTemplate.setAllowWrite( true );

            hostedTemplate.setDownloadRemoteIndexes( false );

            createRepositoryTemplate( hostedTemplate, shouldRecreate );
        }
        else if ( TEMPLATE_DEFAULT_PROXY_RELEASE.equals( id ) )
        {
            getLogger().info( "Creating default proxied release repository template..." );

            CRepository proxiedTemplate = new CRepository();

            proxiedTemplate.setId( TEMPLATE_DEFAULT_PROXY_RELEASE );

            proxiedTemplate.setName( "" );

            proxiedTemplate.setRepositoryPolicy( CRepository.REPOSITORY_POLICY_RELEASE );

            proxiedTemplate.setArtifactMaxAge( -1 );

            proxiedTemplate.setAllowWrite( false );

            proxiedTemplate.setDownloadRemoteIndexes( true );

            proxiedTemplate.setChecksumPolicy( CRepository.CHECKSUM_POLICY_WARN );

            proxiedTemplate.setRemoteStorage( new CRemoteStorage() );

            proxiedTemplate.getRemoteStorage().setUrl( "http://some-remote-repository/repo-root" );

            createRepositoryTemplate( proxiedTemplate, shouldRecreate );
        }
        else if ( TEMPLATE_DEFAULT_PROXY_SNAPSHOT.equals( id ) )
        {
            getLogger().info( "Creating default proxied snapshot repository template..." );

            CRepository proxiedTemplate = new CRepository();

            proxiedTemplate.setId( TEMPLATE_DEFAULT_PROXY_SNAPSHOT );

            proxiedTemplate.setName( "" );

            proxiedTemplate.setRepositoryPolicy( CRepository.REPOSITORY_POLICY_SNAPSHOT );

            proxiedTemplate.setAllowWrite( false );

            proxiedTemplate.setDownloadRemoteIndexes( true );

            proxiedTemplate.setRemoteStorage( new CRemoteStorage() );

            proxiedTemplate.getRemoteStorage().setUrl( "http://some-remote-repository/repo-root" );

            createRepositoryTemplate( proxiedTemplate, shouldRecreate );
        }
        else if ( TEMPLATE_DEFAULT_VIRTUAL.equals( id ) )
        {
            getLogger().info( "Creating default virtual repository template..." );

            CRepositoryShadow shadowTemplate = new CRepositoryShadow();

            shadowTemplate.setId( TEMPLATE_DEFAULT_VIRTUAL );

            shadowTemplate.setName( "" );

            createRepositoryShadowTemplate( shadowTemplate, shouldRecreate );
        }
    }

    // ===========================
    // Event handling

    public void onProximityEvent( AbstractEvent evt )
    {
        try
        {
            if ( evt instanceof RepositoryItemEvent )
            {
                RepositoryItemEvent ievt = (RepositoryItemEvent) evt;

                if ( ievt.getItemUid().getPath().endsWith( ".pom" ) || ievt.getItemUid().getPath().endsWith( ".jar" ) )
                {
                    StorageItem item = ievt.getRepository().retrieveItem( true, ievt.getItemUid() );

                    // filter out links and dirs/collections
                    if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
                    {
                        StorageFileItem pomItem = (StorageFileItem) item;

                        NexusArtifactEvent nae = new NexusArtifactEvent();
                        NexusItemInfo ai = new NexusItemInfo();
                        ai.setRepositoryId( pomItem.getRepositoryId() );
                        ai.setPath( pomItem.getPath() );
                        ai.setRemoteUrl( pomItem.getRemoteUrl() );
                        nae.setNexusItemInfo( ai );
                        nae.setEventDate( ievt.getEventDate() );
                        nae.setEventContext( ievt.getContext() );

                        if ( ievt instanceof RepositoryItemEventCache )
                        {
                            nae.setAction( NexusArtifactEvent.ACTION_CACHED );
                        }
                        else if ( ievt instanceof RepositoryItemEventStore )
                        {
                            nae.setAction( NexusArtifactEvent.ACTION_DEPLOYED );
                        }
                        else if ( ievt instanceof RepositoryItemEventDelete )
                        {
                            nae.setAction( NexusArtifactEvent.ACTION_DELETED );
                        }
                        else if ( ievt instanceof RepositoryItemEventRetrieve )
                        {
                            // this creates a lot of noise in feed
                            // nae.setAction( NexusArtifactEvent.ACTION_RETRIEVED );
                            return;
                        }

                        getFeedRecorder().addNexusArtifactEvent( nae );
                    }

                }
            }
            else if ( evt instanceof RepositoryRegistryEvent )
            {
                RepositoryRegistryEvent revt = (RepositoryRegistryEvent) evt;

                StringBuffer sb = new StringBuffer( " repository " );

                sb.append( revt.getRepository().getName() );

                sb.append( " (ID=" );

                sb.append( revt.getRepository().getId() );

                sb.append( ") " );

                if ( RepositoryType.PROXY.equals( revt.getRepository().getRepositoryType() ) )
                {
                    sb.append( " as proxy repository for URL " );

                    sb.append( revt.getRepository().getRemoteUrl() );
                }
                else if ( RepositoryType.HOSTED.equals( revt.getRepository().getRepositoryType() ) )
                {
                    sb.append( " as hosted repository" );
                }
                else if ( RepositoryType.SHADOW.equals( revt.getRepository().getRepositoryType() ) )
                {
                    sb.append( " as " );

                    sb.append( revt.getRepository().getClass().getName() );

                    sb.append( " virtual repository for " );

                    sb.append( ( (ShadowRepository) revt.getRepository() ).getMasterRepository().getName() );

                    sb.append( " (ID=" );

                    sb.append( ( (ShadowRepository) revt.getRepository() ).getMasterRepository().getId() );

                    sb.append( ") " );
                }

                sb.append( "." );

                if ( revt instanceof RepositoryRegistryEventAdd )
                {
                    sb.insert( 0, "Registered" );
                }
                else if ( revt instanceof RepositoryRegistryEventRemove )
                {
                    sb.insert( 0, "Unregistered" );
                }

                getFeedRecorder().addSystemEvent( FeedRecorder.SYSTEM_CONFIG_ACTION, sb.toString() );
            }
        }
        catch ( Exception e )
        {
            getLogger().warn( "Error during Nexus event handling:", e );
        }
    }

    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        systemStatus.setLastConfigChange( new Date() );

        getFeedRecorder().addSystemEvent( FeedRecorder.SYSTEM_CONFIG_ACTION, "Nexus configuration changed/updated." );
    }

}
