/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.metadata.RuleProviderMetadata;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.ContextBase;

/**
 * {@link AbstractRuleProvider} provides metadata, and a list of {@link Rule} objects that are then evaluated by the
 * {@link RuleSubet} during Windup execution.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class AbstractRuleProvider extends ContextBase implements RuleProvider
{
    private int executionIndex;
    private RuleProviderMetadata metadata;

    /**
     * Create a new {@link AbstractRuleProvider} instance using the given {@link RuleProviderMetadata}.
     */
    public AbstractRuleProvider(RuleProviderMetadata metadata)
    {
        this.metadata = metadata;
    }

    /**
     * Create a new {@link AbstractRuleProvider} instance using the given parameters to construct default
     * {@link RuleProviderMetadata}.
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

        if (!metadata.getID().equals(metadata.getOrigin()))
        {
            builder.append(" from ").append(metadata.getOrigin());
        }

        return builder.toString();
    }
}
