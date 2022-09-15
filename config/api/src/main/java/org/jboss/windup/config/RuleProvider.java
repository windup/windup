package org.jboss.windup.config;

import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleProviderMetadata;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationProvider;
import org.ocpsoft.rewrite.config.Rule;

/**
 * A windup-specific implementation of {@link ConfigurationProvider}. Provides {@link Rule} instances and relevant {@link RuleProviderMetadata} for
 * those {@link Rule} instances.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface RuleProvider extends ConfigurationProvider<RuleLoaderContext> {
    /**
     * Get the {@link RuleProviderMetadata} for this {@link RuleProvider}.
     */
    RuleProviderMetadata getMetadata();

    /**
     * KEEP - The purpose of this override is to make sure that reflection (interface.getClass().getMethods())
     * keeps a method with the specific type. Otherwise, type erasure will get rid of it and callers will get
     * ClassCastExceptions in some obscure cases.
     */
    @Override
    Configuration getConfiguration(RuleLoaderContext context);
}
