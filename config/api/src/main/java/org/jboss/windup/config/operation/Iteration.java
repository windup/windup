/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation;

import java.util.Arrays;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.IterationBuilderComplete;
import org.jboss.windup.config.operation.iteration.IterationBuilderOtherwise;
import org.jboss.windup.config.operation.iteration.IterationBuilderOver;
import org.jboss.windup.config.operation.iteration.IterationBuilderPerform;
import org.jboss.windup.config.operation.iteration.IterationBuilderVar;
import org.jboss.windup.config.operation.iteration.IterationBuilderWhen;
import org.jboss.windup.config.operation.iteration.IterationImpl;
import org.jboss.windup.config.operation.iteration.IterationPayloadManager;
import org.jboss.windup.config.operation.iteration.IterationQuery;
import org.jboss.windup.config.operation.iteration.IterationQueryImpl;
import org.jboss.windup.config.operation.iteration.IterationSelectionManager;
import org.jboss.windup.config.operation.iteration.NamedIterationPayloadManager;
import org.jboss.windup.config.operation.iteration.NamedIterationSelectionManager;
import org.jboss.windup.config.operation.iteration.TypedNamedIterationPayloadManager;
import org.jboss.windup.config.operation.iteration.TypedNamedIterationSelectionManager;
import org.jboss.windup.config.selectables.VarStack;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.config.And;
import org.ocpsoft.rewrite.config.CompositeOperation;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.DefaultOperationBuilder;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public abstract class Iteration extends DefaultOperationBuilder implements IterationBuilderOver, IterationBuilderVar,
            IterationBuilderWhen, IterationBuilderPerform, IterationBuilderOtherwise, IterationBuilderComplete,
            CompositeOperation
{
    private Condition condition;
    private Operation operationPerform;
    private Operation operationOtherwise;

    public abstract IterationSelectionManager getSelectionManager();

    public abstract IterationPayloadManager getPayloadManager();

    public abstract void setPayloadManager(IterationPayloadManager payloadManager);

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
    public IterationQuery queryFor(Class<? extends WindupVertexFrame> varType, String var)
    {
        return new IterationQueryImpl(this, new TypedNamedIterationPayloadManager(varType, var));
    }

    @Override
    public IterationQuery queryFor(String var)
    {
        return new IterationQueryImpl(this, new NamedIterationPayloadManager(var));
    }

    public IterationBuilderWhen all(Condition... condition)
    {
        this.condition = And.all(condition);
        return this;
    }

    /**
     * A condition which decides for each frame whether .perform() 
     * or otherwise() will be processed.
     */
    @Override
    public IterationBuilderWhen when(Condition condition)
    {
        this.condition = condition;
        return this;
    }

    /**
     *  Will be processed for frames which comply to the condition in when().
     */
    @Override
    public IterationBuilderPerform perform(Operation operation)
    {
        this.operationPerform = operation;
        return this;
    }

    /**
     *  Will be processed for frames which DO NOT comply to the condition in when().
     */
    @Override
    public IterationBuilderOtherwise otherwise(Operation operation)
    {
        this.operationOtherwise = operation;
        return this;
    }

    /**
     *  Visual cap of the iteration.
     */
    @Override
    public IterationBuilderComplete endIteration()
    {
        return this;
    }

    /**
     *  Called internally to actually process the Iteration.
     */
    @Override
    public void perform(Rewrite event, EvaluationContext context)
    {
        perform((GraphRewrite) event, context);
    }

    /**
     *  Called internally to actually process the Iteration.
     *  Loops over the frames to iterate, and performs their
     *  .perform( ... ) or .otherwise( ... ) parts.
     */
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        VarStack factory = VarStack.instance(event);
        factory.push();
        Iterable<WindupVertexFrame> frames = getSelectionManager().getFrames(event, factory);
        for (WindupVertexFrame frame : frames)
        {
            getPayloadManager().setCurrentPayload(factory, frame);
            if (condition == null || condition.evaluate(event, context))
            {
                if (operationPerform != null)
                {
                    operationPerform.perform(event, context);
                }
            }
            else if (condition != null)
            {
                if (operationOtherwise != null)
                {
                    operationOtherwise.perform(event, context);
                }
            }
        }
        getPayloadManager().removeCurrentPayload(factory);
        factory.pop();
    }

    @Override
    public List<Operation> getOperations()
    {
        return Arrays.asList(operationPerform, operationOtherwise);
    }
}
