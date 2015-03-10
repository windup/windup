package org.jboss.windup.config.loader;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.RuleProviderRegistry;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;

/**
 * {@link RuleLoader} manages loading {@link Configuration}s from all {@link AbstractRuleProvider}s in the system.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public interface RuleLoader
{
    /**
     * Loads all known {@link AbstractRuleProvider} instances that are accepted by the provided {@link Predicate} and
     * returns the result.
     * 
     * @param ruleProviderFilter Must accept null.
     */
    public RuleProviderRegistry loadConfiguration(GraphContext context, Predicate<RuleProvider> ruleProviderFilter);
}
