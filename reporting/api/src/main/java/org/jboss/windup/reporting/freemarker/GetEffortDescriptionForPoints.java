package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.reporting.service.EffortReportService;
import org.jboss.windup.reporting.service.EffortReportService.Verbosity;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Given a number of points, return a short textual description (eg, Trivial or Complex).
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GetEffortDescriptionForPoints implements WindupFreeMarkerMethod
{
    private static final String NAME = "getEffortDescriptionForPoints";

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
            throw new TemplateModelException("Error, method expects one or two arguments (Integer, [verbosity:String])");
        }
        SimpleNumber simpleNumber = (SimpleNumber) arguments.get(0);
        int effort = simpleNumber.getAsNumber().intValue();

        Verbosity verbosity = Verbosity.SHORT;
        if (arguments.size() > 1)
        {
            final TemplateScalarModel verbosityModel = (TemplateScalarModel) arguments.get(1);
            String verbosityString = verbosityModel.getAsString();
            verbosity = Verbosity.valueOf(verbosityString.toUpperCase());
        }

        String result = EffortReportService.getEffortLevelDescription(verbosity, effort);

        ExecutionStatistics.get().end(NAME);
        return result;
    }
}
