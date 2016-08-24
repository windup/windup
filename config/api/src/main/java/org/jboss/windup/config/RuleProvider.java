package org.jboss.windup.config;

import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleProviderMetadata;
import org.ocpsoft.rewrite.config.ConfigurationProvider;
import org.ocpsoft.rewrite.config.Rule;

/**
 * A windup-specific implementation of {@link ConfigurationProvider}. Provides {@link Rule} instances and relevant {@link RuleProviderMetadata} for
 * those {@link Rule} instances.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface RuleProvider extends ConfigurationProvider<RuleLoaderContext>
{
    /**
     * Get the {@link RuleProviderMetadata} for this {@link RuleProvider}.
     */
    RuleProviderMetadata getMetadata();
}
