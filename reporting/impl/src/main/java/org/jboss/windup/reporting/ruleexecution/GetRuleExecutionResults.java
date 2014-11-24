package org.jboss.windup.reporting.ruleexecution;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.util.ExecutionStatistics;

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

    public static final String NAME = "getRuleExecutionResults";

    private GraphRewrite event;

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes a parameter of type " + WindupRuleProvider.class.getSimpleName() + " and returns a List<"
                    + RuleExecutionInformation.class.getSimpleName() + "> containing metadata related to the current Windup execution.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (WindupRuleProvider)");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        WindupRuleProvider ruleProvider = (WindupRuleProvider) stringModelArg.getWrappedObject();
        List<RuleExecutionInformation> result = RuleExecutionResultsListener.instance(this.event).getRuleExecutionInformation(ruleProvider);
        ExecutionStatistics.get().begin(NAME);
        return result;
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        this.event = event;
    }

}
