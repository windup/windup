/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.exception.IllegalTypeArgumentException;
import org.jboss.windup.config.operation.iteration.IterationBuilderComplete;
import org.jboss.windup.config.operation.iteration.IterationBuilderOtherwise;
import org.jboss.windup.config.operation.iteration.IterationBuilderOver;
import org.jboss.windup.config.operation.iteration.IterationBuilderPerform;
import org.jboss.windup.config.operation.iteration.IterationBuilderVar;
import org.jboss.windup.config.operation.iteration.IterationBuilderWhen;
import org.jboss.windup.config.operation.iteration.IterationPayloadManager;
import org.jboss.windup.config.operation.iteration.NamedFramesSelector;
import org.jboss.windup.config.operation.iteration.NamedIterationPayloadManager;
import org.jboss.windup.config.operation.iteration.TopLayerSingletonFramesSelector;
import org.jboss.windup.config.operation.iteration.TypedFramesSelector;
import org.jboss.windup.config.operation.iteration.TypedNamedFramesSelector;
import org.jboss.windup.config.operation.iteration.TypedNamedIterationPayloadManager;
import org.jboss.windup.config.selectors.FramesSelector;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.Util;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.common.util.Assert;
import org.ocpsoft.rewrite.config.And;
import org.ocpsoft.rewrite.config.CompositeOperation;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilder;
import org.ocpsoft.rewrite.config.DefaultOperationBuilder;
import org.ocpsoft.rewrite.config.NoOp;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.Perform;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

import com.google.common.collect.Iterables;
import org.jboss.windup.util.exception.WindupStopException;
import org.ocpsoft.rewrite.config.CompositeCondition;

/**
 * Used to iterate over an implicit or explicit variable defined within the corresponding {@link ConfigurationRuleBuilder#when(Condition)} clause in
 * the current rule.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="http://ondra.zizka.cz">Ondrej Zizka, I, zizka@seznam.cz</a>
 */
public class Iteration extends DefaultOperationBuilder
            implements IterationBuilderVar, IterationBuilderOver,
            IterationBuilderWhen, IterationBuilderPerform, IterationBuilderOtherwise,
            IterationBuilderComplete, CompositeOperation
{
    private static final String VAR_INSTANCE_STRING = "_instance";
    public static final String DEFAULT_VARIABLE_LIST_STRING = "default";
    public static final String DEFAULT_SINGLE_VARIABLE_STRING = singleVariableIterationName(DEFAULT_VARIABLE_LIST_STRING);

    private Condition condition;
    private Operation operationPerform;
    private Operation operationOtherwise;

    private IterationPayloadManager payloadManager;
    private final FramesSelector selectionManager;

    /**
     * Calculates the default name for the single variable in the selection with the given name.
     */
    public static String singleVariableIterationName(String selectionName)
    {
        return selectionName + VAR_INSTANCE_STRING;
    }

    /**
     * Create a new {@link Iteration}
     */
    private Iteration(FramesSelector selectionManager)
    {
        Assert.notNull(selectionManager, "Selection manager must not be null.");
        this.selectionManager = selectionManager;
    }

    /**
     * Begin an {@link Iteration} over the named selection of the given type. Also sets the name and type of the variable for this iteration's
     * "current element". The type serves for automatic type check.
     */
    public static IterationBuilderOver over(Class<? extends WindupVertexFrame> sourceType, String source)
    {
        Iteration iterationImpl = new Iteration(new TypedNamedFramesSelector(sourceType, source));
        iterationImpl.setPayloadManager(new TypedNamedIterationPayloadManager(sourceType,
                    singleVariableIterationName(source)));
        return iterationImpl;
    }

    /**
     * Begin an {@link Iteration} over the named selection. Also sets the name of the variable for this iteration's "current element".
     */
    public static IterationBuilderOver over(String source)
    {
        Iteration iterationImpl = new Iteration(new NamedFramesSelector(source));
        iterationImpl.setPayloadManager(new NamedIterationPayloadManager(singleVariableIterationName(source)));
        return iterationImpl;
    }

    /**
     * Begin an {@link Iteration} over the selection of the given type, named with the default name. Also sets the name of the variable for this
     * iteration's "current element" to have the default value.
     */
    public static IterationBuilderOver over(Class<? extends WindupVertexFrame> sourceType)
    {
        Iteration iterationImpl = new Iteration(new TypedFramesSelector(sourceType));
        iterationImpl.setPayloadManager(new TypedNamedIterationPayloadManager(sourceType,
                    DEFAULT_SINGLE_VARIABLE_STRING));
        return iterationImpl;
    }

    /**
     * Begin an {@link Iteration} over the selection that is placed on the top of the {@link Variables}. Also sets the name of the variable for this
     * iteration's "current element" (i.e payload) to have the default value.
     */
    public static IterationBuilderOver over()
    {
        Iteration iterationImpl = new Iteration(new TopLayerSingletonFramesSelector());
        iterationImpl.setPayloadManager(new NamedIterationPayloadManager(DEFAULT_SINGLE_VARIABLE_STRING));
        return iterationImpl;
    }

    /**
     * Change the name of the single variable of the given type. If this method is not called, the name is calculated using the
     * {@link Iteration#singleVariableIterationName(String)} method.
     */
    @Override
    public IterationBuilderVar as(Class<? extends WindupVertexFrame> varType, String var)
    {
        setPayloadManager(new TypedNamedIterationPayloadManager(varType, var));
        return this;
    }

    /**
     * Change the name of the single variable. If this method is not called, the name is calculated using the
     * {@link Iteration#singleVariableIterationName(String)} method.
     */
    @Override
    public IterationBuilderVar as(String var)
    {
        setPayloadManager(new NamedIterationPayloadManager(var));
        return this;
    }

    public IterationBuilderWhen all(Condition... condition)
    {
        this.condition = And.all(condition);
        return this;
    }

    @Override
    public IterationBuilderWhen when(Condition condition)
    {
        this.condition = condition;
        return this;
    }

    @Override
    public IterationBuilderPerform perform(Operation operation)
    {
        this.operationPerform = operation;
        return this;
    }

    @Override
    public IterationBuilderPerform perform(Operation... operations)
    {
        this.operationPerform = Perform.all(operations);
        return this;
    }

    @Override
    public IterationBuilderOtherwise otherwise(Operation operation)
    {
        this.operationOtherwise = operation;
        return this;
    }

    /**
     * Visual end of the iteration.
     */
    @Override
    public IterationBuilderComplete endIteration()
    {
        return this;
    }

    /**
     * Called internally to actually process the Iteration.
     */
    @Override
    public void perform(Rewrite event, EvaluationContext context)
    {
        perform((GraphRewrite) event, context);
    }

    /**
     * Called internally to actually process the Iteration. Loops over the frames to iterate, and performs their .perform( ... ) or .otherwise( ... )
     * parts.
     */
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        Variables variables = Variables.instance(event);
        Iterable<? extends WindupVertexFrame> frames = getSelectionManager().getFrames(event, context);

        boolean hasCommitOperation = OperationUtil.hasCommitOperation(operationPerform) || OperationUtil.hasCommitOperation(operationOtherwise);
        boolean hasIterationOperation = OperationUtil.hasIterationProgress(operationPerform)
                    || OperationUtil.hasIterationProgress(operationOtherwise);
        DefaultOperationBuilder commit = new NoOp();
        DefaultOperationBuilder iterationProgressOperation = new NoOp();

        if (!hasCommitOperation || !hasIterationOperation)
        {
            int frameCount = Iterables.size(frames);
            if (frameCount > 100)
            {
                if (!hasCommitOperation)
                    commit = Commit.every(1000);

                if (!hasIterationOperation)
                {
                    // Use 500 here as 100 might be noisy.
                    iterationProgressOperation = IterationProgress.monitoring("Rule Progress", 500);
                }
            }
        }
        Operation commitAndProgress = commit.and(iterationProgressOperation);

        event.getRewriteContext().put(DEFAULT_VARIABLE_LIST_STRING, frames); // set the current frames
        try
        {
            for (WindupVertexFrame frame : frames)
            try
            {
                variables.push();
                getPayloadManager().setCurrentPayload(variables, frame);
                boolean conditionResult = true;
                if (condition != null)
                {
                    final String payloadVariableName = getPayloadVariableName(event, context);
                    passInputVariableNameToConditionTree(condition, payloadVariableName);
                    conditionResult = condition.evaluate(event, context);
                    /*
                     * Add special clear layer for perform, because condition used one and could have added new variables. The condition result put into
                     * variables is ignored.
                     */
                    variables.push();
                    getPayloadManager().setCurrentPayload(variables, frame);
                }
                if (conditionResult)
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
                commitAndProgress.perform(event, context);

                getPayloadManager().removeCurrentPayload(variables);
                // remove the perform layer
                variables.pop();
                if (condition != null)
                {
                    // remove the condition layer
                    variables.pop();
                }
            }
            catch (WindupStopException ex)
            {
                throw new WindupStopException(Util.WINDUP_BRAND_NAME_ACRONYM+" stop requested in " + this.toString(), ex);
            }
            catch (Exception e)
            {
                throw new WindupException("Failed when iterating " + frame.toPrettyString() + ", due to: " + e.getMessage(), e);
            }
        }
        finally
        {
            event.getRewriteContext().put(DEFAULT_VARIABLE_LIST_STRING, null);
        }
    }


    private void passInputVariableNameToConditionTree(Condition condition, String payloadVariableName) throws IllegalStateException
    {
        // Automatically set the input variable to point to the current payload.
        if (condition instanceof GraphCondition)
        {
            ((GraphCondition) condition).setInputVariablesName(payloadVariableName);
        }
        // WINDUP-1057 - we need to pass the variable name manually to the GraphCondition's nested in CompositeCondition's.
        if (condition instanceof CompositeCondition)
        {
            CompositeCondition composite = (CompositeCondition) condition;
            for (Condition childCondition : composite.getConditions())
            {
                passInputVariableNameToConditionTree(childCondition, payloadVariableName);
            }
        }
    }

    @Override
    public List<Operation> getOperations()
    {
        return Arrays.asList(operationPerform, operationOtherwise);
    }

    /**
     * Return the current {@link Iteration} payload variable name.
     *
     * @throws IllegalStateException if there is more than one variable in the {@link Variables} stack, and the payload name cannot be determined.
     */
    public static String getPayloadVariableName(GraphRewrite event, EvaluationContext ctx) throws IllegalStateException
    {
        Variables variables = Variables.instance(event);
        Map<String, Iterable<? extends WindupVertexFrame>> topLayer = variables.peek();
        if (topLayer.keySet().size() != 1)
        {
            throw new IllegalStateException("Cannot determine Iteration payload variable name because the top "
                        + "layer of " + Variables.class.getSimpleName() + " stack contains " + topLayer.keySet().size() + " variables: "
                        + topLayer.keySet());
        }
        String name = topLayer.keySet().iterator().next();
        return name;
    }

    /**
     * Set the current {@link Iteration} payload.
     */
    public static void setCurrentPayload(Variables stack, String name, WindupVertexFrame frame)
                throws IllegalArgumentException
    {
        Map<String, Iterable<? extends WindupVertexFrame>> vars = stack.peek();

        Iterable<? extends WindupVertexFrame> existingValue = vars.get(name);
        if (!(existingValue == null || existingValue instanceof IterationPayload))
        {
            throw new IllegalArgumentException("Variable \"" + name
                        + "\" has already been assigned and cannot be used as an " + Iteration.class.getSimpleName()
                        + " variable.");
        }

        vars.put(name, new IterationPayload<>(frame));
    }

    /**
     * Get the {@link Iteration} payload with the given name.
     *
     * @throws IllegalArgumentException if the given variable refers to a non-payload.
     */
    @SuppressWarnings("unchecked")
    public static <FRAMETYPE extends WindupVertexFrame> FRAMETYPE getCurrentPayload(Variables stack, String name)
                throws IllegalStateException, IllegalArgumentException
    {
        Map<String, Iterable<? extends WindupVertexFrame>> vars = stack.peek();

        Iterable<? extends WindupVertexFrame> existingValue = vars.get(name);
        if (!(existingValue == null || existingValue instanceof IterationPayload))
        {
            throw new IllegalArgumentException("Variable \"" + name
                        + "\" is not an " + Iteration.class.getSimpleName() + " variable.");
        }

        Object object = stack.findSingletonVariable(name);
        return (FRAMETYPE) object;
    }

    /**
     * Get the {@link Iteration} payload with the given name and type.
     *
     * @throws IllegalArgumentException if the given variable refers to a non-payload.
     */
    @SuppressWarnings("unchecked")
    public static <FRAMETYPE extends WindupVertexFrame> FRAMETYPE getCurrentPayload(Variables stack,
                Class<FRAMETYPE> type, String name) throws IllegalStateException, IllegalArgumentException
    {
        Map<String, Iterable<? extends WindupVertexFrame>> vars = stack.peek();

        Iterable<? extends WindupVertexFrame> existingValue = vars.get(name);
        if (!(existingValue == null || existingValue instanceof IterationPayload))
        {
            throw new IllegalArgumentException("Variable \"" + name
                        + "\" is not an " + Iteration.class.getSimpleName() + " variable.");
        }

        Object object = stack.findSingletonVariable(type, name);
        return (FRAMETYPE) object;
    }

    /**
     * Remove the current {@link Iteration} payload.
     */
    public static <FRAMETYPE extends WindupVertexFrame> FRAMETYPE removeCurrentPayload(Variables stack,
                Class<FRAMETYPE> type, String name)
                            throws IllegalStateException, IllegalTypeArgumentException
    {
        FRAMETYPE payload = getCurrentPayload(stack, type, name);

        Map<String, Iterable<? extends WindupVertexFrame>> vars = stack.peek();
        vars.remove(name);

        return payload;
    }

    /**
     * Remove the current {@link Iteration} payload.
     */
    public static <FRAMETYPE extends WindupVertexFrame> FRAMETYPE removeCurrentPayload(Variables stack, String name)
                throws IllegalStateException, IllegalTypeArgumentException
    {
        FRAMETYPE payload = getCurrentPayload(stack, name);

        Map<String, Iterable<? extends WindupVertexFrame>> vars = stack.peek();
        vars.remove(name);

        return payload;
    }

    public void setPayloadManager(IterationPayloadManager payloadManager)
    {
        Assert.notNull(payloadManager, "Payload manager must not be null.");
        this.payloadManager = payloadManager;
    }

    public FramesSelector getSelectionManager()
    {
        return selectionManager;
    }

    public IterationPayloadManager getPayloadManager()
    {
        return payloadManager;
    }

    private static class IterationPayload<T> extends HashSet<T>
    {
        private static final long serialVersionUID = 7725055142596456025L;

        public IterationPayload(T element)
        {
            super(1);
            super.add(element);
        }

        @Override
        public boolean add(T e)
        {
            throw new UnsupportedOperationException("Iteration payloads are not modifiable.");
        }

        @Override
        public boolean remove(Object o)
        {
            throw new UnsupportedOperationException("Iteration payloads are not modifiable.");
        }

        @Override
        public void clear()
        {
            throw new UnsupportedOperationException("Iteration payloads are not modifiable.");
        }

        @Override
        public boolean addAll(Collection<? extends T> c)
        {
            throw new UnsupportedOperationException("Iteration payloads are not modifiable.");
        }

        @Override
        public boolean removeAll(Collection<?> c)
        {
            throw new UnsupportedOperationException("Iteration payloads are not modifiable.");
        }

        @Override
        public boolean retainAll(Collection<?> c)
        {
            throw new UnsupportedOperationException("Iteration payloads are not modifiable.");
        }

    }

    /**
     * @return Description of this iteration, e.g. "Iteration.over(?).as(...).when(...).perform(...)".
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Iteration.over(?)");
        if (!getPayloadManager().getPayLoadName().equals(DEFAULT_SINGLE_VARIABLE_STRING))
        {
            builder.append(".as(").append(getPayloadManager().getPayLoadName()).append(")");
        }
        if (condition != null)
        {
            builder.append(".when(").append(condition).append(")");
        }
        if (operationPerform != null)
        {
            builder.append(".perform(").append(operationPerform).append(")");
        }
        if (operationOtherwise != null)
        {
            builder.append(".otherwise(").append(operationOtherwise).append(")");
        }
        return builder.toString();
    }
}
