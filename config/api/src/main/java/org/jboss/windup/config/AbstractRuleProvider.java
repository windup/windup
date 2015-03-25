/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import org.jboss.forge.furnace.util.Annotations;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.RuleMetadataType;
import org.jboss.windup.config.metadata.RuleProviderMetadata;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.ContextBase;

/**
 * {@link AbstractRuleProvider} provides metadata, and a list of {@link Rule} objects that are then evaluated by the {@link RuleSubset} during Windup
 * execution.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class AbstractRuleProvider extends ContextBase implements RuleProvider
{
    private int executionIndex;
    private RuleProviderMetadata metadata;

    public AbstractRuleProvider()
    {
        if (!Annotations.isAnnotationPresent(getClass(), RuleMetadata.class))
        {
            throw new IllegalStateException(getClass().getName() + " must either "
                    + "be abstract, or specify @" + RuleMetadata.class.getName()
                        + ", or call a super() constructor and provide " + RuleProviderMetadata.class.getName());
        }
        this.metadata = MetadataBuilder.forProvider(getClass());
    }

    /**
     * Create a new {@link AbstractRuleProvider} instance using the given {@link RuleProviderMetadata}.
     */
    public AbstractRuleProvider(RuleProviderMetadata metadata)
    {
        this.metadata = metadata;
    }

    /**
     * Create a new {@link AbstractRuleProvider} instance using the given parameters to construct default {@link RuleProviderMetadata}.
     */
    public AbstractRuleProvider(Class<? extends RuleProvider> implementationType, String id)
    {
        this.metadata = MetadataBuilder.forProvider(implementationType, id);
    }

    @Override
    public RuleProviderMetadata getMetadata()
    {
        return metadata;
    }

    @Override
    public boolean handles(Object payload)
    {
        return payload instanceof GraphContext;
    }

    /**
     * Specify additional meta-data to individual {@link Rule} instances originating from the corresponding {@link RuleProvider} instance.
     */
    public static void enhanceRuleMetadata(RuleProvider provider, Rule rule)
    {
        if (rule instanceof Context)
        {
            Context context = (Context) rule;
            if (!context.containsKey(RuleMetadataType.ORIGIN))
                context.put(RuleMetadataType.ORIGIN, provider.getMetadata().getOrigin());
            if (!context.containsKey(RuleMetadataType.RULE_PROVIDER))
                context.put(RuleMetadataType.RULE_PROVIDER, provider);
            if (!context.containsKey(RuleMetadataType.TAGS))
                context.put(RuleMetadataType.TAGS, provider.getMetadata().getTags());
        }
    }

    @Override
    public boolean equals(Object other)
    {
        boolean result = false;
        if (other instanceof AbstractRuleProvider)
        {
            AbstractRuleProvider that = (AbstractRuleProvider) other;
            result = this.getMetadata().equals(that.getMetadata());
        }
        return result;
    }

    @Override
    public int hashCode()
    {
        return getMetadata().hashCode();
    }

    /**
     * For internal use only.
     */
    public final int getExecutionIndex()
    {
        return executionIndex;
    }

    /**
     * For internal use only.
     */
    public final void setExecutionIndex(int executionIndex)
    {
        this.executionIndex = executionIndex;
    }

    /**
     * The "priority" of the {@link RuleProvider} instance. This is not presently used by Windup.
     */
    @Override
    public final int priority()
    {
        return 0;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(metadata.getID());

        if (this instanceof RulePhase || !metadata.getID().equals(metadata.getOrigin()))
        {
            builder.append(" from ").append(metadata.getOrigin());
        }

        return builder.toString();
    }
}
