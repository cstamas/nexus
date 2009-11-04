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
package org.sonatype.nexus.configuration.validator;

import java.util.List;

import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.model.CErrorReporting;
import org.sonatype.nexus.configuration.model.CHttpProxySettings;
import org.sonatype.nexus.configuration.model.CMirror;
import org.sonatype.nexus.configuration.model.CPathMappingItem;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteNexusInstance;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.CRestApiSettings;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CScheduleConfig;
import org.sonatype.nexus.configuration.model.CScheduledTask;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;

/**
 * The validator used to validate current configuration in boot-up sequence.
 * 
 * @author cstamas
 * @deprecated see Configurable
 */
public interface ApplicationConfigurationValidator
    extends ConfigurationValidator
{
    /**
     * Validates a repository configuration.
     * 
     * @param repository
     * @return
     */
    ValidationResponse validateRepository( ApplicationValidationContext ctx, CRepository repository );

    /**
     * Validates remote connection settings.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRemoteConnectionSettings( ApplicationValidationContext ctx,
        CRemoteConnectionSettings settings );

    /**
     * Validates remote proxy settings.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRemoteHttpProxySettings( ApplicationValidationContext ctx,
        CRemoteHttpProxySettings settings );

    /**
     * Validates remote authentication.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRemoteAuthentication( ApplicationValidationContext ctx, CRemoteAuthentication settings );

    /**
     * Validates rest api settings.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRestApiSettings( ApplicationValidationContext ctx, CRestApiSettings settings );

    /**
     * Validates Nexus built-in HTTP proxy settings.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateHttpProxySettings( ApplicationValidationContext ctx, CHttpProxySettings settings );

    /**
     * Validates routing.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRouting( ApplicationValidationContext ctx, CRouting settings );

    /**
     * Validates remote nexus instance.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRemoteNexusInstance( ApplicationValidationContext ctx, CRemoteNexusInstance settings );

    /**
     * Validates repository grouping.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRepositoryGrouping( ApplicationValidationContext ctx, CRepositoryGrouping settings );

    /**
     * Validates mapping.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateGroupsSettingPathMappingItem( ApplicationValidationContext ctx, CPathMappingItem settings );

    /**
     * Validates repository target item.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRepositoryTarget( ApplicationValidationContext ctx, CRepositoryTarget settings );

    /**
     * Validates scheduled task.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateScheduledTask( ApplicationValidationContext ctx, CScheduledTask settings );

    /**
     * Validates schedule.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateSchedule( ApplicationValidationContext ctx, CScheduleConfig settings );

    /**
     * Validates smtp
     * 
     * @param ctx
     * @param settings
     * @return
     */
    ValidationResponse validateSmtpConfiguration( ApplicationValidationContext ctx, CSmtpConfiguration settings );

    /**
     * Validate mirror config
     * 
     * @param ctx
     * @param mirror
     * @return
     */
    ValidationResponse validateRepositoryMirrors( ApplicationValidationContext ctx, List<CMirror> mirrors );
    
    /**
     * Validates error reporting
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateErrorReporting( ApplicationValidationContext ctx, CErrorReporting settings );
}
