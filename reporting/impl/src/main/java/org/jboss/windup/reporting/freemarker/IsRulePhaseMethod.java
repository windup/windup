package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.phase.RulePhase;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * Returns true if the passed in object is a and instanceof {@link RulePhase}.<br/>
 * 
 * isRulePhase(Object):boolean
 *
 */
public class IsRulePhaseMethod implements WindupFreeMarkerMethod
{

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        if (arguments.size() != 1)
        {
            throw new TemplateModelException(
                        "Error, method expects one argument (Object)");
        }
        StringModel stringModel = (StringModel) arguments.get(0);
        Object object = stringModel.getWrappedObject();
        return object instanceof RulePhase;
    }

    @Override
    public String getMethodName()
    {
        return "isRulePhase";
    }

    @Override
    public String getDescription()
    {
        return "Returns true if the passed in object is an instance of " + RulePhase.class.getSimpleName() + ".";
    }

    @Override
    public void setContext(GraphRewrite event)
    {

    }

}
