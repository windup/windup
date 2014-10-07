package org.jboss.windup.config.loader;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.WindupRuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;

/**
 * {@link WindupRuleLoader} manages loading {@link Configuration}s from all {@link WindupRuleProvider}s in the system.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public interface WindupRuleLoader
{
    /**
     * Loads all known {@link WindupRuleProvider} instances that are accepted by the provided {@link Predicate} and returns the result.
     * 
     * @param ruleProviderFilter Must accept null.
     */
    public WindupRuleMetadata loadConfiguration(GraphContext context, Predicate<WindupRuleProvider> ruleProviderFilter);
}
