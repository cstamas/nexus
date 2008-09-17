package org.sonatype.nexus.jsecurity.realms;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * @plexus.component role="org.jsecurity.realm.Realm" role-hint="NexusTargetRealm"
 *
 */
public class NexusTargetRealm
    extends NexusMethodRealm
{
    public static final String PRIVILEGE_TYPE_TARGET = "target";
    
    public static final String PRIVILEGE_PROPERTY_REPOSITORY_TARGET = "repositoryTargetId";
    public static final String PRIVILEGE_PROPERTY_REPOSITORY_ID = "repositoryId";
    public static final String PRIVILEGE_PROPERTY_REPOSITORY_GROUP_ID = "repositoryGroupId";
    /**
     * @plexus.requirement
     */
    private Nexus nexus;
    
    @Override
    protected Set<Permission> getPermissions( String privilegeId )
    {
        CPrivilege privilege = getConfigurationManager().readPrivilege( privilegeId );
        
        if ( privilege == null )
        {
            return Collections.emptySet();
        }
        
        if ( !privilege.getType().equals( PRIVILEGE_TYPE_TARGET ) )
        {
            return super.getPermissions( privilegeId );
        }
        
        String repositoryTarget = getConfigurationManager().getPrivilegeProperty( privilege, PRIVILEGE_PROPERTY_REPOSITORY_TARGET );
        String method = getConfigurationManager().getPrivilegeProperty( privilege, PRIVILEGE_PROPERTY_METHOD );
        String repositoryId = getConfigurationManager().getPrivilegeProperty( privilege, PRIVILEGE_PROPERTY_REPOSITORY_ID );
        String repositoryGroupId = getConfigurationManager().getPrivilegeProperty( privilege, PRIVILEGE_PROPERTY_REPOSITORY_GROUP_ID );
     
        StringBuilder basePermString = new StringBuilder();
        
        basePermString.append( "nexus:target:" );            
        basePermString.append( repositoryTarget );            
        basePermString.append( ":" );
        
        StringBuilder postPermString = new StringBuilder();
        
        postPermString.append( ":" );
        
        if ( StringUtils.isEmpty( method ) )
        {
            postPermString.append( "*" );
        }
        else
        {
            postPermString.append( method );
        }
        
        if ( !StringUtils.isEmpty( repositoryId ) )
        {
            return Collections.singleton( ( Permission ) new WildcardPermission( basePermString + repositoryId + postPermString ) );
        }
        else if ( !StringUtils.isEmpty( repositoryGroupId ) )
        {
            try
            {
                Set<Permission> permissions = new HashSet<Permission>();
                
                List<Repository> repositories = nexus.getRepositoryGroup( repositoryGroupId );
                
                for ( Repository repository : repositories )
                {
                    WildcardPermission permission = new WildcardPermission( basePermString + repository.getId() + postPermString );

                    permissions.add( permission );
                }
                
                return permissions;
            }
            catch ( NoSuchRepositoryGroupException e )
            {
                // If there is no such group you don't have permission to it
                return Collections.emptySet();
            }
        }
        else
        {
            return Collections.singleton( ( Permission ) new WildcardPermission( basePermString + "*" + postPermString ) );
        }
    }
}
