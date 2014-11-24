package org.jboss.windup.reporting.ruleexecution;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.WindupRuleMetadata;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.template.TemplateModelException;

/**
 * Returns a {@link List} of all {@link WindupRuleProvider}s loaded by Windup.
 * 
 * Can be called from Freemarker as follows:
 * 
 * getAllRuleProviders()
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class GetAllRuleProviders implements WindupFreeMarkerMethod
{
    private static final String NAME = "getAllRuleProviders";

    private GraphRewrite event;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.event = event;
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes no parameters and returns a List<" + WindupRuleProvider.class.getSimpleName() + "> containing all loaded Rule Providers.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        List<WindupRuleProvider> result = WindupRuleMetadata.instance(this.event).getProviders();
        ExecutionStatistics.get().end(NAME);
        return result;
    }

}
