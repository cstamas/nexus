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
package org.sonatype.nexus.feeds;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.timeline.TimelineFilter;

/**
 * A recorder for events for later retrieval. The Actions are "generic" Nexus event related. For specific (Maven, P2)
 * actions, look into specific sources. Note: This is actually event recorder, not feed recorder.
 * 
 * @author cstamas
 */
public interface FeedRecorder
{
    /**
     * System event action: boot
     */
    public static final String SYSTEM_BOOT_ACTION = "BOOT";

    /**
     * System event action: configuration
     */
    public static final String SYSTEM_CONFIG_ACTION = "CONFIG";

    /**
     * System event action: timeline purge
     */
    public static final String SYSTEM_TL_PURGE_ACTION = "TL_PURGE";

    /**
     * System event action: reindex
     */
    public static final String SYSTEM_REINDEX_ACTION = "REINDEX";

    /**
     * System event action: publish indexes
     */
    public static final String SYSTEM_PUBLISHINDEX_ACTION = "PUBLISHINDEX";

    /**
     * System event action: download indexes
     */
    public static final String SYSTEM_DOWNLOADINDEX_ACTION = "DOWNLOADINDEX";

    /**
     * System event action: rebuildAttributes
     */
    public static final String SYSTEM_REBUILDATTRIBUTES_ACTION = "REBUILDATTRIBUTES";

    /**
     * System event action: repository local status changes
     */
    public static final String SYSTEM_REPO_LSTATUS_CHANGES_ACTION = "REPO_LSTATUS_CHANGES";

    /**
     * System event action: repository proxy status auto change
     */
    public static final String SYSTEM_REPO_PSTATUS_CHANGES_ACTION = "REPO_PSTATUS_CHANGES";

    /**
     * System event action: repository proxy status auto change
     */
    public static final String SYSTEM_REPO_PSTATUS_AUTO_CHANGES_ACTION = "REPO_PSTATUS_AUTO_CHANGES";

    /**
     * System event action: expire cache
     */
    public static final String SYSTEM_EXPIRE_CACHE_ACTION = "EXPIRE_CACHE";

    /**
     * System event action: shadow sync
     */
    public static final String SYSTEM_SYNC_SHADOW_ACTION = "SYNC_SHADOW";

    /**
     * System event action: evict unused proxied items
     */
    public static final String SYSTEM_EVICT_UNUSED_PROXIED_ITEMS_ACTION = "EVICT_UNUSED_PROXIED_ITEMS";

    /**
     * System event action: empty trash
     */
    public static final String SYSTEM_EMPTY_TRASH_ACTION = "EMPTY_TRASH";

    /**
     * System event action: remove repository folder
     */
    public static final String SYSTEM_REMOVE_REPO_FOLDER_ACTION = "REMOVE_REPO_FOLDER";
    
    /**
     * System event action: authentication
     */
    public static final String SYSTEM_AUTHC = "AUTHC";
    
    /**
     * System event action: authorization
     */
    public static final String SYSTEM_AUTHZ = "AUTHZ";

    // creating

    void addErrorWarningEvent( String action, String message);
    
    void addErrorWarningEvent( String action, String message, Throwable throwable);
    
    void addNexusArtifactEvent( NexusArtifactEvent nae );

    void addSystemEvent( String action, String message );

    void addAuthcAuthzEvent( AuthcAuthzEvent evt );

    SystemProcess systemProcessStarted( String action, String message );

    void systemProcessFinished( SystemProcess prc, String finishMessage );

    void systemProcessBroken( SystemProcess prc, Throwable e );

    // reading

    List<Map<String, String>> getEvents( Set<String> types, Set<String> subtypes, Integer from, Integer count,
        TimelineFilter filter );

    List<NexusArtifactEvent> getNexusArtifectEvents( Set<String> subtypes, Integer from, Integer count,
        TimelineFilter filter );
    
    List<NexusArtifactEvent> getNexusArtifactEvents( Set<String> subtypes, Long ts, Integer count, 
        TimelineFilter filter );

    List<SystemEvent> getSystemEvents( Set<String> subtypes, Integer from, Integer count, TimelineFilter filter );

    List<AuthcAuthzEvent> getAuthcAuthzEvents( Set<String> subtypes, Integer from, Integer count, TimelineFilter filter );
    
    List<AuthcAuthzEvent> getAuthcAuthzEvents( Set<String> subtypes, Long ts, Integer count, TimelineFilter filter );
    
    List<ErrorWarningEvent> getErrorWarningEvents( Set<String> subtypes, Integer from, Integer count, TimelineFilter filter );

    List<ErrorWarningEvent> getErrorWarningEvents( Set<String> subtypes, Long ts, Integer count, TimelineFilter filter );
}
