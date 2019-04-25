package org.jboss.windup.config.metadata;

import org.jboss.windup.config.loader.RuleLoaderContext;

import java.util.Collection;

public interface LabelLoader
{
    Collection<Label> loadLabels(RuleLoaderContext ruleLoaderContext);
}
