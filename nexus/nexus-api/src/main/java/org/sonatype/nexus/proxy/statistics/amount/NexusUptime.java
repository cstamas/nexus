package org.sonatype.nexus.proxy.statistics.amount;

import java.util.Date;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.proxy.statistics.Stat;

public class NexusUptime
    implements Stat<Long>
{
    private final ApplicationStatusSource applicationStatusSource;

    public NexusUptime( final ApplicationStatusSource applicationStatusSource )
    {
        this.applicationStatusSource = applicationStatusSource;
    }

    @Override
    public String getName()
    {
        return "uptime";
    }

    @Override
    public Long value()
    {
        Date started = applicationStatusSource.getSystemStatus().getStartedAt();

        return System.currentTimeMillis() - started.getTime();
    }
}
