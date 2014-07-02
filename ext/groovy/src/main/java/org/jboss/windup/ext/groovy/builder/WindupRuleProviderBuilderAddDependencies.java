package org.jboss.windup.ext.groovy.builder;

import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderCustom;

public interface WindupRuleProviderBuilderAddDependencies
{
    public WindupRuleProviderBuilderAddDependencies addDependency(String dependencyID);

    public ConfigurationRuleBuilderCustom addRule();
}
