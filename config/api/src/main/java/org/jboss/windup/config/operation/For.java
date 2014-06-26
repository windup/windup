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
import org.jboss.windup.config.operation.foreach.ForBuilderComplete;
import org.jboss.windup.config.operation.foreach.ForBuilderOtherwise;
import org.jboss.windup.config.operation.foreach.ForBuilderIn;
import org.jboss.windup.config.operation.foreach.ForBuilderPerform;
import org.jboss.windup.config.operation.foreach.ForBuilderEach;
import org.jboss.windup.config.operation.foreach.ForBuilderWhen;
import org.jboss.windup.config.operation.foreach.ForImpl;
import org.jboss.windup.config.operation.foreach.GremlinQueryCriteria;
import org.jboss.windup.config.operation.foreach.GremlinQueryImpl;
import org.jboss.windup.config.operation.iteration.IterationPayloadManager;
import org.jboss.windup.config.operation.iteration.GremlinPipesQuery;
import org.jboss.windup.config.operation.iteration.GremlinPipesQueryImpl;
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
 * For.each("var").in("javaClasses") [.when()] .perform(...) [.otherwise(...)] .endFor();
 * 
 * @author Ondrej Zizka, ozizka@redhat.com
 * @see https://issues.jboss.org/browse/WINDUP-93
 */
public abstract class For extends DefaultOperationBuilder implements 
        ForBuilderEach, ForBuilderIn,
        ForBuilderWhen, ForBuilderPerform, ForBuilderOtherwise, ForBuilderComplete,
        CompositeOperation, IterationRoot
{
    private Condition condition;
    private Operation operationPerform;
    private Operation operationOtherwise;

    public abstract IterationSelectionManager getSelectionManager();

    public abstract IterationPayloadManager getPayloadManager();

    public abstract void setSelectionManager(IterationSelectionManager selManager);

        
    
    /**
     * Sets the name and type of the variable for this iteration's "current element".
     * The type server for automatic type check.
     * Similar to {@link Iteration.var(Class<? extends WindupVertexFrame> varType, String var)}.
     */
    public static ForBuilderEach each(Class<? extends WindupVertexFrame> varType, String var)
    {
        ForImpl forImpl = new ForImpl();
        forImpl.setPayloadManager(new TypedNamedIterationPayloadManager(varType, var));
        return forImpl;
    }

    /**
     * Sets the name of the variable for this iteration's "current element".
     * Similar to {@link Iteration.var(String var)}.
     */
    public ForBuilderEach each(String var)
    {
        ForImpl forImpl = new ForImpl();
        forImpl.setPayloadManager(new NamedIterationPayloadManager(var));
        return forImpl;
    }

    /**
     * Begin an {@link Iteration} over the named selection.
     */
    public ForBuilderIn in(String var)
    {
        this.setSelectionManager( new NamedIterationSelectionManager(var) );
        return this;
    }

    /**
     * Begin an {@link Iteration} over the named selection of the given type.
     */
    public ForBuilderIn in(Class<? extends WindupVertexFrame> sourceType, String source)
    {
        this.setSelectionManager(new TypedNamedIterationSelectionManager(sourceType, source));
        return this;
    }


    @Override
    public GremlinPipesQuery queryFor()
    {
        // TODO - I'd prefer if we used Iteration.over(...).var(...).queryFor()...
        //        It would make the API more comprehensible and clearer.
        return new GremlinPipesQueryImpl(this, this.getPayloadManager());
    }

    @Override
    public GremlinQueryCriteria from(String gremlinQuery)
    {
        return new GremlinQueryImpl(this, gremlinQuery);
    }

    public ForBuilderWhen all(Condition... condition)
    {
        this.condition = And.all(condition);
        return this;
    }

    /**
     * A condition which decides for each frame whether .perform() 
     * or otherwise() will be processed.
     */
    @Override
    public ForBuilderWhen when(Condition condition)
    {
        this.condition = condition;
        return this;
    }

    /**
     *  Will be processed for frames which comply to the condition in when().
     */
    @Override
    public ForBuilderPerform perform(Operation operation)
    {
        this.operationPerform = operation;
        return this;
    }

    /**
     *  Will be processed for frames which DO NOT comply to the condition in when().
     */
    @Override
    public ForBuilderOtherwise otherwise(Operation operation)
    {
        this.operationOtherwise = operation;
        return this;
    }

    /**
     *  Visual cap of the iteration.
     */
    @Override
    public ForBuilderComplete endFor()
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
        VarStack varStack = VarStack.instance(event);
        varStack.push();
        Iterable<WindupVertexFrame> frames = getSelectionManager().getFrames(event, varStack);
        for (WindupVertexFrame frame : frames)
        {
            getPayloadManager().setCurrentPayload(varStack, frame);
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
        getPayloadManager().removeCurrentPayload(varStack);
        varStack.pop();
    }

    @Override
    public List<Operation> getOperations()
    {
        return Arrays.asList(operationPerform, operationOtherwise);
    }
}
