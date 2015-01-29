package org.jboss.windup.config.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Vetoed;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderCustom;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderWithMetadata;
import org.ocpsoft.rewrite.config.Rule;
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
    private Class<? extends RulePhase> rulePhase = WindupRuleProvider.DEFAULT_PHASE;

    private List<String> executeAfterIDs = new ArrayList<>();
    private List<Class<? extends WindupRuleProvider>> executeAfterTypes = new ArrayList<>();
    private List<String> executeBeforeIDs = new ArrayList<>();
    private List<Class<? extends WindupRuleProvider>> executeBeforeTypes = new ArrayList<>();

    private Map<Object, Object> ruleMetadata = new HashMap<>();
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
    public WindupRuleProviderBuilderAddDependencies setPhase(Class<? extends RulePhase> phase)
    {
        this.rulePhase = phase;
        return this;
    }

    @Override
    public WindupRuleProviderBuilderAddDependencies addExecuteAfter(String id)
    {
        executeAfterIDs.add(id);
        return this;
    }

    @Override
    public WindupRuleProviderBuilderAddDependencies addExecuteAfter(Class<? extends WindupRuleProvider> type)
    {
        executeAfterTypes.add(type);
        return this;
    }

    @Override
    public WindupRuleProviderBuilderAddDependencies addExecuteBefore(String id)
    {
        executeBeforeIDs.add(id);
        return this;
    }

    @Override
    public WindupRuleProviderBuilderAddDependencies addExecuteBefore(Class<? extends WindupRuleProvider> type)
    {
        executeBeforeTypes.add(type);
        return this;
    }

    public WindupRuleProviderBuilderAddDependencies withMetadata(Object key, Object value)
    {
        ruleMetadata.put(key, value);
        return this;
    }

    @Override
    public ConfigurationRuleBuilderCustom addRule()
    {
        ConfigurationRuleBuilderCustom rule = configurationBuilder.addRule();
        ConfigurationRuleBuilderWithMetadata ruleWithMetadata = (ConfigurationRuleBuilderWithMetadata) rule;
        for (Map.Entry<Object, Object> metadataEntry : ruleMetadata.entrySet())
        {
            ruleWithMetadata.withMetadata(metadataEntry.getKey(), metadataEntry.getValue());
        }
        return rule;
    }

    @Override
    public ConfigurationRuleBuilderCustom addRule(Rule rule)
    {
        ConfigurationRuleBuilderCustom wrapped = configurationBuilder.addRule(rule);
        ConfigurationRuleBuilderWithMetadata ruleWithMetadata = (ConfigurationRuleBuilderWithMetadata) wrapped;
        for (Map.Entry<Object, Object> metadataEntry : ruleMetadata.entrySet())
        {
            ruleWithMetadata.withMetadata(metadataEntry.getKey(), metadataEntry.getValue());
        }
        return wrapped;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return configurationBuilder;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        super.enhanceMetadata(context);
        if (this.metadataEnhancer != null)
            metadataEnhancer.accept(context);
    }

    @Override
    public String getID()
    {
        return id;
    }

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return rulePhase;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return executeAfterTypes;
    }

    @Override
    public List<String> getExecuteAfterIDs()
    {
        return executeAfterIDs;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
    {
        return executeBeforeTypes;
    }

    @Override
    public List<String> getExecuteBeforeIDs()
    {
        return executeBeforeIDs;
    }

    @Override
    public String toString()
    {
        return this.getID();
    }
}
