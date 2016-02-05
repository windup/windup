package org.jboss.windup.reporting.freemarker;

import java.util.List;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.reporting.service.EffortReportService;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;

/**
 * Given a number of points, return a short textual description (eg, Trivial or Complex).
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GetEffortDescriptionForPoints implements WindupFreeMarkerMethod
{
    private static final String NAME = "getEffortDescriptionForPoints";

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Given a number of points, return a short textual description (eg, Trivial or Complex).";
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() < 1)
        {
            throw new TemplateModelException("Error, method expects one or two arguments (Integer, [verbose:boolean])");
        }
        SimpleNumber simpleNumber = (SimpleNumber) arguments.get(0);
        int effort = simpleNumber.getAsNumber().intValue();

        boolean verbose = false;
        if (arguments.size() > 1)
            verbose = ((TemplateBooleanModel) arguments.get(1)).getAsBoolean();

        Map<Integer, String> effortToDescription = verbose ? EffortReportService.getVerboseEffortLevelDescriptionMappings()
                    : EffortReportService.getEffortLevelDescriptionMappings();
        String result = effortToDescription.containsKey(effort) ? effortToDescription.get(effort) : EffortReportService.UNKNOWN;

        ExecutionStatistics.get().end(NAME);
        return result;
    }

    @Override
    public void setContext(GraphRewrite event)
    {

    }
}
