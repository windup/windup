package org.jboss.windup.config;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.graph.GraphContext;

/**
 * Loads and executes the Windup Configuration.
 * 
 */
public interface ConfigurationProcessor
{
    /**
     * Executes all available {@link Configuration}s on the provided {@link GraphContext}
     */
    public void run(GraphContext context);

    /**
     * Executes all {@link Configuration}s that are accepted by the provided filter on the provided {@link GraphContext}
     */
    public void run(GraphContext context, Predicate<WindupRuleProvider> ruleProviderFilter);
}
