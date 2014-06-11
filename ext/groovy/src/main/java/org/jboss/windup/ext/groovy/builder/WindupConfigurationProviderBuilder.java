package org.jboss.windup.ext.groovy.builder;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.ext.groovy.GroovyConfigurationProvider;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderCustom;

public class WindupConfigurationProviderBuilder implements WindupConfigurationProviderBuilderSetPhase,
            WindupConfigurationProviderBuilderAddDependencies
{
    private String id;
    private RulePhase rulePhase;
    private List<String> dependencies = new ArrayList<>();
    private ConfigurationBuilder configurationBuilder;

    public WindupConfigurationProviderBuilder(String id)
    {
        this.id = id;
        configurationBuilder = ConfigurationBuilder.begin();
    }

    public WindupConfigurationProvider getWindupConfigurationProvider()
    {
        return new GroovyConfigurationProvider(id, rulePhase, dependencies, configurationBuilder);
    }

    public static WindupConfigurationProviderBuilderSetPhase buildWindupRule(String id)
    {
        return new WindupConfigurationProviderBuilder(id);
    }

    @Override
    public WindupConfigurationProviderBuilderAddDependencies setPhase(RulePhase phase)
    {
        this.rulePhase = phase;
        return this;
    }

    @Override
    public WindupConfigurationProviderBuilderAddDependencies addDependency(String dependencyID)
    {
        dependencies.add(dependencyID);
        return this;
    }

    @Override
    public ConfigurationRuleBuilderCustom addRule()
    {
        return configurationBuilder.addRule();
    }
}
