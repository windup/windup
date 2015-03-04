package org.jboss.windup.config.metadata;

import org.jboss.windup.config.RuleProvider;

/**
 * Describes {@link RuleProvider} instances.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface RuleProviderMetadata extends RulesetMetadata
{
    /**
     * Returns the {@link Class} of the corresponding {@link RuleProvider}.
     */
    Class<? extends RuleProvider> getType();
}
