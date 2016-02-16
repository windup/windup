package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.reporting.service.EffortReportService;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import org.jboss.windup.reporting.service.EffortReportService.Verbosity;

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

        Verbosity verbosity = Verbosity.SHORT;
        if (arguments.size() > 1)
        {
            final Object arg2 = arguments.get(1);
            // Support for getEffortDescriptionForPoints( 10, true )
            if( arg2 instanceof TemplateBooleanModel && ((TemplateBooleanModel) arg2).getAsBoolean())
                verbosity = Verbosity.VERBOSE;
            // Support for getEffortDescriptionForPoints( 10, 'verbose' ) or 'id'
            if( arg2 instanceof TemplateScalarModel ){
                String asString = ((TemplateScalarModel)arg2).getAsString();
                if(asString.equals("verbose"))
                    verbosity = Verbosity.VERBOSE;
                if(asString.equals("id"))
                    verbosity = Verbosity.ID;
            }
        }

        String result = EffortReportService.getEffortLevelDescription(verbosity, effort);

        ExecutionStatistics.get().end(NAME);
        return result;
    }

    @Override
    public void setContext(GraphRewrite event)
    {

    }
}
