package org.jboss.windup.config.builder;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Vetoed;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderCustom;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderWithMetadata;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Used to construct new dynamic {@link AbstractRuleProvider} instances.
 * 
 * @author jsightler
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Vetoed
public final class WindupRuleProviderBuilder extends AbstractRuleProvider implements
            WindupRuleProviderBuilderSetPhase,
            WindupRuleProviderBuilderMetadataSetPhase,
            WindupRuleProviderBuilderAddDependencies
{
    private Map<Object, Object> ruleMetadata = new HashMap<>();

    private ConfigurationBuilder configurationBuilder;
    private MetadataBuilder metadata;

    /**
     * Begin creating a new dynamic {@link AbstractRuleProvider}.
     */
    public static WindupRuleProviderBuilder begin(Class<? extends RuleProvider> implementationType, String id)
    {
        MetadataBuilder builder = MetadataBuilder.forProvider(implementationType, id);
        return new WindupRuleProviderBuilder(builder).setMetadataBuilder(builder);
    }

    private WindupRuleProviderBuilder(MetadataBuilder builder)
    {
        super(builder);
        configurationBuilder = ConfigurationBuilder.begin();
    }

    private WindupRuleProviderBuilder setMetadataBuilder(MetadataBuilder builder)
    {
        this.metadata = builder;
        return this;
    }

    public void setOrigin(String origin)
    {
        metadata.setOrigin(origin);
    }

    @Override
    public WindupRuleProviderBuilderAddDependencies setPhase(Class<? extends RulePhase> phase)
    {
        metadata.setPhase(phase);
        return this;
    }

    @Override
    public WindupRuleProviderBuilderAddDependencies addExecuteAfter(String id)
    {
        metadata.addExecuteAfterId(id);
        return this;
    }

    @Override
    public WindupRuleProviderBuilderAddDependencies addExecuteAfter(Class<? extends AbstractRuleProvider> type)
    {
        metadata.addExecuteAfter(type);
        return this;
    }

    @Override
    public WindupRuleProviderBuilderAddDependencies addExecuteBefore(String id)
    {
        metadata.addExecuteBeforeId(id);
        return this;
    }

    @Override
    public WindupRuleProviderBuilderAddDependencies addExecuteBefore(Class<? extends AbstractRuleProvider> type)
    {
        metadata.addExecuteBefore(type);
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
    public String toString()
    {
        return super.toString();
    }
}
