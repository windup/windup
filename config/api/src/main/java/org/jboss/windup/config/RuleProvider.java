package org.jboss.windup.config;

import org.jboss.windup.config.metadata.RuleProviderMetadata;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.ConfigurationProvider;
import org.ocpsoft.rewrite.config.Rule;

/**
 * A windup-specific implementation of {@link ConfigurationProvider}. Provides {@link Rule} instances and relevant {@link RuleProviderMetadata} for
 * those {@link Rule} instances.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface RuleProvider extends ConfigurationProvider<GraphContext>
{
    /**
     * Get the {@link RuleProviderMetadata} for this {@link RuleProvider}.
     */
    public RuleProviderMetadata getMetadata();
}
