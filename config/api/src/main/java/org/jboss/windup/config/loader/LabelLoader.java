package org.jboss.windup.config.loader;

import org.jboss.windup.config.metadata.LabelProviderRegistry;

/**
 * Loads all {@link LabelProviderLoader}s in the system.
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
public interface LabelLoader
{
    /**
     * Loads all known {@link LabelProviderLoader}
     */
    LabelProviderRegistry loadConfiguration(RuleLoaderContext ruleLoaderContext);
}
