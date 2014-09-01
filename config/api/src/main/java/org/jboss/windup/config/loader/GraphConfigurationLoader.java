package org.jboss.windup.config.loader;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;

/**
 * {@link GraphConfigurationLoader} manages loading {@link Configuration}s from all {@link WindupRuleProvider}s in the
 * system.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public interface GraphConfigurationLoader
{
    /**
     * Loads all OCP Rewrite's {@link Configuration}s from the system and returns the result.
     */
    public Configuration loadConfiguration(GraphContext context);

    /**
     * Loads all OCP Rewrite's {@link Configuration}s that are passed by the provided {@link Predicate} and returns the result.
     */
    public Configuration loadConfiguration(GraphContext context, Predicate<WindupRuleProvider> ruleProviderFilter);
}
