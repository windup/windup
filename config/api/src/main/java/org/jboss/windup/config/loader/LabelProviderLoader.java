package org.jboss.windup.config.loader;

import org.jboss.windup.config.LabelProvider;

import java.util.List;

public interface LabelProviderLoader
{
    boolean isFileBased();
    List<LabelProvider> getProviders(RuleLoaderContext ruleLoaderContext);
}
