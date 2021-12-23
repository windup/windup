package org.jboss.windup.config.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;

import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Maintains a link between all {@link Rule} and {@link RuleProvider} instances that have been loaded by Windup.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class RuleProviderRegistry
{
    private final List<RuleProvider> providers = new ArrayList<>();
    private final IdentityHashMap<RuleProvider, List<Rule>> providersToRules = new IdentityHashMap<>();
    private Configuration configuration;

    /**
     * Gets the current instance of {@link RuleProviderRegistry}.
     */
    public static RuleProviderRegistry instance(GraphRewrite event)
    {
        return (RuleProviderRegistry) event.getRewriteContext().get(RuleProviderRegistry.class);
    }

    /**
     * Sets the list of loaded {@link RuleProvider}s.
     */
    public void setProviders(List<RuleProvider> providers)
    {
        this.providers.clear();
        this.providers.addAll(providers);
    }

    /**
     * Gets the list of loaded {@link RuleProvider}s as an immutable {@link List}.
     */
    public List<RuleProvider> getProviders()
    {
        return Collections.unmodifiableList(providers);
    }

    /**
     * Sets the {@link List} of {@link Rule}s that were loaded from the given {@link RuleProvider}.
     */
    public void addRulesForProvider(RuleProvider provider, List<Rule> rules)
    {
        providersToRules.put(provider, rules);
    }

    /**
     * Gets all of the {@link Rule}s that were loaded by the given {@link RuleProvider}.
     */
    public List<Rule> getRules(RuleProvider provider)
    {
        List<Rule> rules = providersToRules.get(provider);
        if (rules == null)
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(rules);
    }

    /**
     * Contains the {@link Configuration} containing all of the loaded {@Rule}s.
     */
    public Configuration getConfiguration()
    {
        return configuration;
    }

    /**
     * Contains the {@link Configuration} containing all of the loaded {@Rule}s.
     */
    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }
}
