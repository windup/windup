package org.jboss.windup.config.loader;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.graph.GraphContext;

/**
 * Each configuration extension will implement this interface to provide a list of WindupRuleProviders. A
 * GraphConfigurationLoader will pull in all WindupRuleProviders and sort them based upon the provider's metadata.
 * 
 * @author jsightler
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface WindupRuleProviderLoader
{
    /**
     * Return all {@link WindupRuleProvider} instances that are relevant for this loader.
     */
    public List<WindupRuleProvider> getProviders(GraphContext context);
}
