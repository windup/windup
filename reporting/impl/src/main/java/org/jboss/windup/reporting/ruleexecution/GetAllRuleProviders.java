package org.jboss.windup.reporting.ruleexecution;

import java.util.List;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.RuleProviderRegistry;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.template.TemplateModelException;

/**
 * Returns a {@link List} of all {@link AbstractRuleProvider}s loaded by Windup.
 * <p>
 * Can be called from Freemarker as follows:
 * <p>
 * getAllRuleProviders()
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GetAllRuleProviders implements WindupFreeMarkerMethod {
    private static final String NAME = "getAllRuleProviders";

    private GraphRewrite event;

    @Override
    public void setContext(GraphRewrite event) {
        this.event = event;
    }

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Takes no parameters and returns a List<" + AbstractRuleProvider.class.getSimpleName() + "> containing all loaded Rule Providers.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);
        List<RuleProvider> result = RuleProviderRegistry.instance(this.event).getProviders();
        ExecutionStatistics.get().end(NAME);
        return result;
    }

}
