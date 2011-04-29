package org.sonatype.nexus.proxy.statistics;

import org.sonatype.nexus.proxy.statistics.Sampler.Frequency;

public interface FrequencyHinted
{
    Frequency getPreferredFrequency();
}
