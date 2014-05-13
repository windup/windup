/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.operation;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.operation.iteration.IterationBuilderComplete;
import org.jboss.windup.addon.config.operation.iteration.IterationBuilderOver;
import org.jboss.windup.addon.config.operation.iteration.IterationBuilderVar;
import org.jboss.windup.addon.config.operation.iteration.IterationBuilderWhen;
import org.jboss.windup.addon.config.operation.iteration.IterationImpl;
import org.jboss.windup.addon.config.operation.iteration.IterationPayloadManager;
import org.jboss.windup.addon.config.operation.iteration.IterationQuery;
import org.jboss.windup.addon.config.operation.iteration.IterationQueryImpl;
import org.jboss.windup.addon.config.operation.iteration.IterationSelectionManager;
import org.jboss.windup.addon.config.operation.iteration.NamedIterationPayloadManager;
import org.jboss.windup.addon.config.operation.iteration.NamedIterationSelectionManager;
import org.jboss.windup.addon.config.operation.iteration.TypedIterationPayloadManager;
import org.jboss.windup.addon.config.operation.iteration.TypedIterationSelectionManager;
import org.jboss.windup.addon.config.operation.iteration.TypedNamedIterationPayloadManager;
import org.jboss.windup.addon.config.operation.iteration.TypedNamedIterationSelectionManager;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.model.meta.WindupVertexFrame;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.DefaultOperationBuilder;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public abstract class Iteration extends DefaultOperationBuilder implements IterationBuilderOver,
            IterationBuilderComplete, IterationBuilderWhen, IterationBuilderVar
{
    private Condition condition;
    private Operation operation;

    /*
     * Abstract methods.
     */
    public abstract IterationSelectionManager getSelectionManager();

    public abstract IterationPayloadManager getPayloadManager();

    public abstract void setPayloadManager(IterationPayloadManager payloadManager);

    /**
     * Begin an {@link Iteration} over the current selection of the given type.
     */
    public static IterationBuilderOver over(Class<? extends WindupVertexFrame> sourceType)
    {
        return new IterationImpl(new TypedIterationSelectionManager(sourceType));
    }

    /**
     * Begin an {@link Iteration} over the named selection of the given type.
     */
    public static IterationBuilderOver over(Class<? extends WindupVertexFrame> sourceType, String source)
    {
        return new IterationImpl(new TypedNamedIterationSelectionManager(sourceType, source));
    }

    /**
     * Begin an {@link Iteration} over the named selection.
     */
    public static IterationBuilderOver over(String source)
    {
        return new IterationImpl(new NamedIterationSelectionManager(source));
    }

    @Override
    public IterationBuilderVar var(Class<? extends WindupVertexFrame> varType)
    {
        setPayloadManager(new TypedIterationPayloadManager(varType));
        return this;
    }

    @Override
    public IterationBuilderVar var(Class<? extends WindupVertexFrame> varType, String var)
    {
        setPayloadManager(new TypedNamedIterationPayloadManager(varType, var));
        return this;
    }

    @Override
    public IterationBuilderVar var(String var)
    {
        setPayloadManager(new NamedIterationPayloadManager(var));
        return this;
    }

    @Override
    public IterationQuery queryFor(Class<? extends WindupVertexFrame> varType)
    {
        return new IterationQueryImpl(this, new TypedIterationPayloadManager(varType));
    }

    @Override
    public IterationQuery queryFor(Class<? extends WindupVertexFrame> varType, String var)
    {
        return new IterationQueryImpl(this, new TypedNamedIterationPayloadManager(varType, var));
    }

    @Override
    public IterationQuery queryFor(String var)
    {
        return new IterationQueryImpl(this, new NamedIterationPayloadManager(var));
    }

    @Override
    public IterationBuilderWhen when(Condition condition)
    {
        this.condition = condition;
        return this;
    }

    @Override
    public IterationBuilderComplete perform(Operation operation)
    {
        this.operation = operation;
        return this;
    }

    @Override
    public void perform(Rewrite event, EvaluationContext context)
    {
        perform((GraphRewrite) event, context);
    }

    public void perform(GraphRewrite event, EvaluationContext context)
    {
        if (operation != null)
        {
            SelectionFactory factory = SelectionFactory.instance(event);
            Iterable<WindupVertexFrame> frames = getSelectionManager().getFrames(event, factory);
            for (WindupVertexFrame element : frames)
            {
                getPayloadManager().setCurrentPayload(factory, element);
                if (condition == null || condition.evaluate(event, context))
                {
                    if (operation != null)
                        operation.perform(event, context);
                }
            }
            getPayloadManager().removeCurrentPayload(factory);
        }
    }
}
