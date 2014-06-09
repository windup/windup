package org.jboss.windup.ext.groovy.builder;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilder;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderCustom;
import org.ocpsoft.rewrite.config.Rule;

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
        return new WindupConfigurationProvider()
        {
            private ConfigurationRuleBuilder completedConfiguration = null;

            {
                ConfigurationBuilder builder = ConfigurationBuilder.begin();
                for (Rule rule : configurationBuilder.getRules())
                {
                    if (completedConfiguration == null)
                    {
                        completedConfiguration = builder.addRule(rule);
                    }
                    else
                    {
                        completedConfiguration = completedConfiguration.addRule(rule);
                    }
                }
            }

            @Override
            public Configuration getConfiguration(GraphContext context)
            {

                return completedConfiguration;
            }

            @Override
            public RulePhase getPhase()
            {
                return rulePhase;
            }
        };
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
