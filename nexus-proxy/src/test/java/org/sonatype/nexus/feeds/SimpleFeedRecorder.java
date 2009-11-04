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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.timeline.TimelineFilter;

@Component( role = FeedRecorder.class )
public class SimpleFeedRecorder
    implements FeedRecorder

{

    public void addNexusArtifactEvent( NexusArtifactEvent nae )
    {
    }

    public void addSystemEvent( String action, String message )
    {
    }

    public void systemProcessBroken( SystemProcess prc, Throwable e )
    {
    }

    public void systemProcessFinished( SystemProcess prc, String finishMessage )
    {
    }

    public void addAuthcAuthzEvent( AuthcAuthzEvent evt )
    {
    }

    public SystemProcess systemProcessStarted( String action, String message )
    {
        return new SystemProcess( action, message, new Date() );
    }

    public void startService()
        throws StartingException
    {
        // TODO Auto-generated method stub

    }

    public void stopService()
        throws StoppingException
    {
        // TODO Auto-generated method stub

    }

    public List<Map<String, String>> getEvents( Set<String> types, Set<String> subtypes, Integer from, Integer count,
                                                TimelineFilter filter )
    {
        return Collections.emptyList();
    }

    public List<NexusArtifactEvent> getNexusArtifectEvents( Set<String> subtypes, Integer from, Integer count,
                                                            TimelineFilter filter )
    {
        return Collections.emptyList();
    }

    public List<NexusArtifactEvent> getNexusArtifactEvents( Set<String> subtypes, Long ts, Integer count,
                                                            TimelineFilter filter )
    {
        return Collections.emptyList();
    }

    public List<SystemEvent> getSystemEvents( Set<String> subtypes, Integer from, Integer count, TimelineFilter filter )
    {
        return Collections.emptyList();
    }

    public List<AuthcAuthzEvent> getAuthcAuthzEvents( Set<String> subtypes, Integer from, Integer count,
                                                      TimelineFilter filter )
    {
        return Collections.emptyList();
    }

    public List<AuthcAuthzEvent> getAuthcAuthzEvents( Set<String> subtypes, Long ts, Integer count,
                                                      TimelineFilter filter )
    {
        return Collections.emptyList();
    }

    public void addErrorWarningEvent( String action, String message )
    {
    }

    public void addErrorWarningEvent( String action, String message, Throwable throwable )
    {
    }

    public List<ErrorWarningEvent> getErrorWarningEvents( Set<String> subtypes, Integer from, Integer count,
                                                          TimelineFilter filter )
    {
        return Collections.emptyList();
    }

    public List<ErrorWarningEvent> getErrorWarningEvents( Set<String> subtypes, Long ts, Integer count,
                                                          TimelineFilter filter )
    {
        return Collections.emptyList();
    }

}
