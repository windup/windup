package org.jboss.windup.config.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Maintains metadata regarding the {@link Rule}s and {@link WindupRuleProvider}s that have been loaded by Windup.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class WindupRuleMetadata
{
    private final List<WindupRuleProvider> providers = new ArrayList<>();
    private final IdentityHashMap<WindupRuleProvider, List<Rule>> providersToRules = new IdentityHashMap<>();
    private Configuration configuration;

    /**
     * Gets the current instance of {@link WindupRuleMetadata}.
     */
    public static WindupRuleMetadata instance(GraphRewrite event)
    {
        WindupRuleMetadata instance = (WindupRuleMetadata) event.getRewriteContext().get(WindupRuleMetadata.class);
        return instance;
    }

    /**
     * Sets the list of loaded {@link WindupRuleProvider}s.
     */
    public void setProviders(List<WindupRuleProvider> providers)
    {
        this.providers.clear();
        this.providers.addAll(providers);
    }

    /**
     * Gets the list of loaded {@link WindupRuleProvider}s as an immutable {@link List}.
     */
    public List<WindupRuleProvider> getProviders()
    {
        return Collections.unmodifiableList(providers);
    }

    /**
     * Sets the {@link List} of {@link Rule}s that were loaded from the given {@link WindupRuleProvider}.
     */
    public void setRules(WindupRuleProvider provider, List<Rule> rules)
    {
        providersToRules.put(provider, rules);
    }

    /**
     * Gets all of the {@link Rule}s that were loaded by the given {@link WindupRuleProvider}.
     */
    public List<Rule> getRules(WindupRuleProvider provider)
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
