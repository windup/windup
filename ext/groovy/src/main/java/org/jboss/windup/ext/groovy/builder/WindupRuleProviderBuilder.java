package org.jboss.windup.ext.groovy.builder;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.ext.groovy.GroovyRuleProvider;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderCustom;

public class WindupRuleProviderBuilder implements WindupRuleProviderBuilderSetPhase,
            WindupRuleProviderBuilderAddDependencies
{
    private String id;
    private RulePhase rulePhase;
    private List<String> dependencies = new ArrayList<>();
    private ConfigurationBuilder configurationBuilder;

    public WindupRuleProviderBuilder(String id)
    {
        this.id = id;
        configurationBuilder = ConfigurationBuilder.begin();
    }

    public WindupRuleProvider getWindupRuleProvider()
    {
        return new GroovyRuleProvider(id, rulePhase, dependencies, configurationBuilder);
    }

    public static WindupRuleProviderBuilderSetPhase buildWindupRule(String id)
    {
        return new WindupRuleProviderBuilder(id);
    }

    @Override
    public WindupRuleProviderBuilderAddDependencies setPhase(RulePhase phase)
    {
        this.rulePhase = phase;
        return this;
    }

    @Override
    public WindupRuleProviderBuilderAddDependencies addDependency(String dependencyID)
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
