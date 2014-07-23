package org.jboss.windup.config.builder;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Vetoed;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderCustom;
import org.ocpsoft.rewrite.context.Context;

/**
 * Used to construct new dynamic {@link WindupRuleProvider} instances.
 * 
 * @author jsightler
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Vetoed
public final class WindupRuleProviderBuilder extends WindupRuleProvider implements
            WindupRuleProviderBuilderSetPhase,
            WindupRuleProviderBuilderMetadataSetPhase,
            WindupRuleProviderBuilderAddDependencies
{
    private String id;
    private RulePhase rulePhase;
    private List<String> dependencies = new ArrayList<>();
    private List<Class<? extends WindupRuleProvider>> typeDependencies = new ArrayList<>();
    private ConfigurationBuilder configurationBuilder;
    private Predicate<Context> metadataEnhancer;

    /**
     * Begin creating a new dynamic {@link WindupRuleProvider}.
     */
    public static WindupRuleProviderBuilder begin(String id)
    {
        return new WindupRuleProviderBuilder(id);
    }

    private WindupRuleProviderBuilder(String id)
    {
        this.id = id;
        configurationBuilder = ConfigurationBuilder.begin();
    }

    @Override
    public WindupRuleProviderBuilderMetadataSetPhase setMetadataEnhancer(Predicate<Context> enhancer)
    {
        this.metadataEnhancer = enhancer;
        return this;
    }

    @Override
    public WindupRuleProviderBuilderAddDependencies setPhase(RulePhase phase)
    {
        this.rulePhase = phase;
        return this;
    }

    @Override
    public WindupRuleProviderBuilderAddDependencies addDependency(String id)
    {
        dependencies.add(id);
        return this;
    }

    @Override
    public WindupRuleProviderBuilderAddDependencies addDependency(Class<? extends WindupRuleProvider> type)
    {
        typeDependencies.add(type);
        return this;
    }

    @Override
    public ConfigurationRuleBuilderCustom addRule()
    {
        return configurationBuilder.addRule();
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return configurationBuilder;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        if (this.metadataEnhancer != null)
            metadataEnhancer.accept(context);
    }

    @Override
    public String getID()
    {
        return id;
    }

    @Override
    public RulePhase getPhase()
    {
        return rulePhase;
    }
}
