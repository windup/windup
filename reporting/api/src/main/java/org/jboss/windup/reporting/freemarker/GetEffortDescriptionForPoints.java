package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.template.SimpleNumber;
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
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (Integer)");
        }
        SimpleNumber simpleNumber = (SimpleNumber) arguments.get(0);
        String result = "";
        switch (simpleNumber.getAsNumber().intValue())
        {
        case 0:
            result = "Info";
            break;
        case 1:
            result = "Trivial";
            break;
        case 3:
            result = "Complex";
            break;
        case 5:
            result = "Redesign";
            break;
        case 7:
            result = "Requires Architectural Change";
            break;
        case 13:
        default:
            result = "Unknown";
        }

        ExecutionStatistics.get().end(NAME);
        return result;
    }

    @Override
    public void setContext(GraphRewrite event)
    {

    }
}
