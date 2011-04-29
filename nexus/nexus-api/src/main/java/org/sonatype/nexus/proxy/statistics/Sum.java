package org.sonatype.nexus.proxy.statistics;

public class Sum<T extends Number>
    extends AbstractStat<Double>
{
    private final Stat<T>[] stats;

    public Sum( final String name, final Stat<T>... stat )
    {
        super( name );

        this.stats = stat;
    }

    @Override
    public Double value()
    {
        Double sum = 0d;

        for ( Stat<T> stat : stats )
        {
            sum += stat.value().doubleValue();
        }

        return sum;
    }
}
