package org.jboss.windup.ext.groovy.builder;

import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderCustom;

public interface WindupConfigurationProviderBuilderAddDependencies
{
    public WindupConfigurationProviderBuilderAddDependencies addDependency(String dependencyID);

    public ConfigurationRuleBuilderCustom addRule();
}
