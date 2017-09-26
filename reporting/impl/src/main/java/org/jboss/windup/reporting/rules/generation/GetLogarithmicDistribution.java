package org.jboss.windup.reporting.rules.generation;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.reporting.model.TagModel;
import org.jboss.windup.reporting.model.TechReportPunchCardModel;
import org.jboss.windup.util.ExecutionStatistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @return Returns a logarithmic distribution usable for visualising ranges that tend to be wide-scale with exponential distribution.
 *         Value samples 0 for 0, 0.25 under around 20 %, 0.50 around 65%, 1.0 around 90 %.
 * @param count    Count of occurences
 * @param maximum  The maximal count of occurences across apps.
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class GetLogarithmicDistribution implements WindupFreeMarkerMethod
{
    public static final Logger LOG = Logger.getLogger(GetLogarithmicDistribution.class.getName());
    private static final String NAME = "getLogaritmicDistribution";

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return"Returns a logarithmic distribution usable for visualising ranges that tend to be wide-scale with exponential distribution.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        if (arguments.size() < 2)
            throw new TemplateModelException("Expected 2 arguments, count and maximum count.");

        if (!(arguments.get(0) instanceof TemplateNumberModel))
            throw new TemplateModelException("Both arguments must be numbers, but the first was " + arguments.get(0).getClass().getName());
        if (!(arguments.get(1) instanceof TemplateNumberModel))
            throw new TemplateModelException("Both arguments must be numbers, but the second was " + arguments.get(1).getClass().getName());

        int count = ((TemplateNumberModel) arguments.get(0)).getAsNumber().intValue();
        if (count < 1)
            return new SimpleNumber(0);

        int maximum = ((TemplateNumberModel) arguments.get(1)).getAsNumber().intValue();
        if (maximum < 0)
            throw new TemplateModelException("Maximum must be at least 0, " + maximum);

        if (count > maximum)
            throw new TemplateModelException("Count " + count + " is larger than maximum " + maximum + ".");

        double ratio = count / maximum; // 0..1

        // Map it to scale 1..1000.
        double ratio2 = 1 + ratio * 999;
        double log10 = Math.log10(ratio2) / 4; // 0..3.999 -> 0..0.999
        return new SimpleNumber(log10);
    }

}
