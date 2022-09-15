package org.jboss.windup.reporting.rules.generation.techreport;

import freemarker.ext.beans.NumberModel;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 * @return Returns a logarithmic distribution usable for visualising ranges that tend to be wide-scale with exponential distribution.
 * Value samples 0 for 0, 0.25 under around 20 %, 0.50 around 65%, 1.0 around 90 %.
 * param count    Count of occurences
 * param maximum  The maximal count of occurences across apps.
 */
public class GetLogarithmicDistribution implements WindupFreeMarkerMethod {
    public static final Logger LOG = Logger.getLogger(GetLogarithmicDistribution.class.getName());
    private static final String NAME = "getLogaritmicDistribution";

    /**
     * This makes the log curve a bit flatter, i.e. the count won't reach the top as easily.
     */
    private static final double FLATTENER = 0.4d;

    /* This prevents counts in small maximums reching large values. I.e. 2 in 10 will be smaller than 200 in 1000.
     * The logic is that it's probably not desirable to show all huge circles if there are just 2 EJBs in each app. */
    private static final double QUITE_A_LOT_FACTOR = 80;

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Returns a logarithmic distribution usable for visualising ranges that tend to be wide-scale with exponential distribution.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        if (arguments.size() < 2)
            throw new TemplateModelException(NAME + ": Expected 2 arguments, count and maximum count.");

        if (!(arguments.get(0) instanceof TemplateNumberModel))
            throw new TemplateModelException(NAME + ": Both arguments must be numbers, but the first was " + arguments.get(0).getClass().getName());
        if (!(arguments.get(1) instanceof TemplateNumberModel))
            throw new TemplateModelException(NAME + ": Both arguments must be numbers, but the second was " + arguments.get(1).getClass().getName());

        int count = ((TemplateNumberModel) arguments.get(0)).getAsNumber().intValue();
        if (count < 1)
            return new SimpleNumber(0);

        int maximum = ((TemplateNumberModel) arguments.get(1)).getAsNumber().intValue();
        if (maximum < 0)
            throw new TemplateModelException(NAME + "Maximum must be at least 0, " + maximum);

        if (count > maximum) {
            LOG.severe("Count " + count + " is larger than maximum " + maximum + ". Using the maximum as count.");
            count = maximum;
        }

        double ratio = ((double) count) / ((double) maximum + QUITE_A_LOT_FACTOR); //  <0..1>

        // Map it to scale 1..1000.
        double ratio2 = 1.0d + ratio * (998d * (1 - FLATTENER));
        double log10 = Math.log10(ratio2) / 3D;    // 0..2.999  =>  0..0.999
        //LOG.info(String.format("count: %d, max: %d, ratio %f, ratio2 %f, log10 %f", count, maximum, ratio, ratio2, log10));
        return new NumberModel(Double.valueOf(log10), new DefaultObjectWrapper());
    }

}
