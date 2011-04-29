package org.sonatype.nexus.proxy.statistics;

public class Ratio<T extends Number>
    extends AbstractSampledStat<Double>
{
    private final Stat<T> numerator;

    private final Stat<T> denominator;

    public Ratio( final String name, final Stat<T> numerator, final Stat<T> denominator )
    {
        super( name, 0d );
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public Double doSample()
    {
        double numeratorValue = numerator.value().doubleValue();
        double denominatorValue = denominator.value().doubleValue();

        if ( ( denominatorValue == 0 ) || ( denominatorValue == Double.NaN ) || ( numeratorValue == Double.NaN ) )
        {
            return 0d;
        }

        return numeratorValue / denominatorValue;
    }
}
