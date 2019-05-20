package org.jboss.windup.config.loader;

import org.jboss.windup.config.metadata.LabelProviderRegistry;

public interface LabelLoader
{
    LabelProviderRegistry loadConfiguration(RuleLoaderContext ruleLoaderContext);
}
