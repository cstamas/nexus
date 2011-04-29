package org.sonatype.nexus.proxy.statistics.amount;

import org.sonatype.nexus.proxy.statistics.Stat;
import org.sonatype.nexus.proxy.statistics.Sum;

public class IncomingRequestsPerSecondCountingStat
    extends Sum<Long>
{
    public IncomingRequestsPerSecondCountingStat( String name, Stat<Long>[] stat )
    {
        super( name, stat );
    }
}
