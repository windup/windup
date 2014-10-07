package org.jboss.windup.reporting.ruleexecution;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * Returns information about which {@link Rule}s have been evaluated by windup as well as their execution results.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class GetRuleExecutionResults implements WindupFreeMarkerMethod
{

    public static final String METHOD_NAME = "getRuleExecutionResults";

    private GraphRewrite event;

    @Override
    public String getMethodName()
    {
        return METHOD_NAME;
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (WindupRuleProvider)");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        WindupRuleProvider ruleProvider = (WindupRuleProvider) stringModelArg.getWrappedObject();
        return RuleExecutionResultsListener.instance(this.event).getRuleExecutionInformation(ruleProvider);
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        this.event = event;
    }

}
