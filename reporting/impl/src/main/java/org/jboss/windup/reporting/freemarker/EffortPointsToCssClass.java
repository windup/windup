package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModelException;

/**
 * Converts from a number of effort points (story points) to a Css class.<br/>
 * 
 * 0 == info<br/>
 * 0-7 == warning<br/>
 * 8-13 == severe<br/>
 * >13 == critical<br/>
 * 
 * @author jsightler
 *
 */
public class EffortPointsToCssClass implements WindupFreeMarkerMethod
{
    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (int)");
        }
        SimpleNumber freemarkerArg = (SimpleNumber) arguments.get(0);
        int effortPoints = freemarkerArg.getAsNumber().intValue();
        if (effortPoints == 0)
            return "info";
        else if (effortPoints < 8)
            return "warning";
        else if (effortPoints < 14)
            return "severe";
        else
            return "critical";
    }

    @Override
    public String getMethodName()
    {
        return "effortPointsToCssClass";
    }

    @Override
    public String getDescription()
    {
        return "Converts from effort points to a CSS class";
    }

    @Override
    public void setContext(GraphRewrite event)
    {
    }

}
