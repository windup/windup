/*
 * Copyright 2014 <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.windup.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import org.jboss.forge.furnace.spi.ListenerRegistration;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.performance.RulePhaseExecutionStatisticsModel;
import org.jboss.windup.graph.model.performance.RuleProviderExecutionStatisticsModel;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.common.util.Assert;
import org.ocpsoft.rewrite.bind.Binding;
import org.ocpsoft.rewrite.bind.Evaluation;
import org.ocpsoft.rewrite.config.CompositeOperation;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionVisit;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.DefaultOperationBuilder;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.OperationVisit;
import org.ocpsoft.rewrite.config.ParameterizedCallback;
import org.ocpsoft.rewrite.config.ParameterizedConditionVisitor;
import org.ocpsoft.rewrite.config.ParameterizedOperationVisitor;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.config.RuleBuilder;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.ContextBase;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.context.RewriteState;
import org.ocpsoft.rewrite.event.Rewrite;
import org.ocpsoft.rewrite.param.ConfigurableParameter;
import org.ocpsoft.rewrite.param.Constraint;
import org.ocpsoft.rewrite.param.DefaultParameter;
import org.ocpsoft.rewrite.param.DefaultParameterStore;
import org.ocpsoft.rewrite.param.Parameter;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.param.Transposition;
import org.ocpsoft.rewrite.util.ParameterUtils;
import org.ocpsoft.rewrite.util.Visitor;

/**
 * An {@link Operation} that allows for conditional evaluation of nested {@link Rule} sets.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class RuleSubset extends DefaultOperationBuilder implements CompositeOperation, Parameterized
{
    private static Logger log = Logger.getLogger(RuleSubset.class.getName());
    

    /**
     * Used for tracking the time taken by the rules within each RuleProvider
     */
    private final IdentityHashMap<WindupRuleProvider, RuleProviderExecutionStatisticsModel> timeTakenByProvider = new IdentityHashMap<>();
    
    /**
     * Used for tracking the time taken by each phase of execution
     */
    private final Map<RulePhase, RulePhaseExecutionStatisticsModel> timeTakenByPhase = new HashMap<>();

    private final Configuration config;

    private List<RuleLifecycleListener> listeners = new ArrayList<>();

    
    
    private RuleSubset(Configuration config)
    {
        Assert.notNull(config, "Configuration must not be null.");
        this.config = config;
    }

    
    public static RuleSubset create(Configuration config)
    {
        return new RuleSubset(config);
    }

    /**
     * Logs the time taken by this rule, and attaches this to the total for the RuleProvider
     */
    private void logTimeTakenByRuleProvider(GraphContext graphContext, Context context, int ruleIndex, int timeTaken)
    {
        WindupRuleProvider ruleProvider = (WindupRuleProvider) context.get(RuleMetadata.RULE_PROVIDER);
        if (ruleProvider == null)
            return;

        if (!timeTakenByProvider.containsKey(ruleProvider))
        {
            RuleProviderExecutionStatisticsModel model = graphContext
                        .getService(RuleProviderExecutionStatisticsModel.class).create();
            model.setRuleIndex(ruleIndex);
            model.setRuleProviderID(ruleProvider.getID());
            model.setTimeTaken(timeTaken);

            timeTakenByProvider.put(ruleProvider, model);
        }
        else
        {
            RuleProviderExecutionStatisticsModel model = timeTakenByProvider.get(ruleProvider);
            int prevTimeTaken = model.getTimeTaken();
            model.setTimeTaken(prevTimeTaken + timeTaken);
        }
        logTimeTakenByPhase(graphContext, ruleProvider.getPhase(), timeTaken);
    }

    /**
     * Logs the time taken by this rule and adds this to the total time taken for this phase
     */
    private void logTimeTakenByPhase(GraphContext graphContext, RulePhase phase, int timeTaken)
    {
        if (!timeTakenByPhase.containsKey(phase))
        {
            RulePhaseExecutionStatisticsModel model = graphContext.getService(RulePhaseExecutionStatisticsModel.class)
                        .create();
            model.setRulePhase(phase.toString());
            model.setTimeTaken(timeTaken);
            timeTakenByPhase.put(phase, model);
        }
        else
        {
            RulePhaseExecutionStatisticsModel model = timeTakenByPhase.get(phase);
            int prevTimeTaken = model.getTimeTaken();
            model.setTimeTaken(prevTimeTaken + timeTaken);
        }
    }

    /*
     * Executors
     */
    @Override
    public void perform(Rewrite rewrite, EvaluationContext context)
    {
        if (!(rewrite instanceof GraphRewrite))
            throw new IllegalArgumentException("Rewrite must be an instanceof GraphRewrite");

        /*
         * Highly optimized loop - for performance reasons. Think before you change this!
         */
        GraphRewrite event = (GraphRewrite) rewrite;

        List<Rule> rules = config.getRules();

        for (RuleLifecycleListener listener : listeners)
        {
            listener.beforeExecution();
        }

        final EvaluationContextImpl subContext = new EvaluationContextImpl();
        for (int i = 0; i < rules.size(); i++)
        {
            Rule rule = rules.get(i);

            Context ruleContext = rule instanceof Context ? (Context) rule : null;
            long ruleTimeStarted = System.currentTimeMillis();
            try
            {

                subContext.clear();
                subContext.put(ParameterStore.class, context.get(ParameterStore.class));
                ParameterValueStore values = (ParameterValueStore) context.get(ParameterValueStore.class);
                subContext.put(ParameterValueStore.class, values);
                subContext.setState(RewriteState.EVALUATING);
                subContext.put(Rule.class, rule);

                event.selectionPush();
                try
                {
                    for (RuleLifecycleListener listener : listeners)
                    {
                        listener.beforeRuleEvaluation(rule, subContext);
                    }

                    if (rule.evaluate(event, subContext))
                    {
                        for (RuleLifecycleListener listener : listeners)
                        {
                            listener.afterRuleConditionEvaluation(event, subContext, rule, true);
                        }

                        if (!handleBindings(event, subContext, values))
                            continue;

                        subContext.setState(RewriteState.PERFORMING);
                        final Object ruleProviderDesc = ((RuleBuilder)rule).get(RuleMetadata.RULE_PROVIDER);
                        log.info("Rule [" + ruleProviderDesc + "] matched and will be performed.");

                        for (RuleLifecycleListener listener : listeners)
                        {
                            listener.beforeRuleOperationsPerformed(event, subContext, rule);
                        }

                        List<Operation> preOperations = subContext.getPreOperations();
                        for (Operation preOperation : preOperations)
                        {
                            preOperation.perform(event, subContext);
                        }

                        if (event.getFlow().isHandled())
                            break;

                        rule.perform(event, subContext);


                        for (RuleLifecycleListener listener : listeners)
                        {
                            listener.afterRuleOperationsPerformed(event, subContext, rule);
                        }

                        if (event.getFlow().isHandled())
                            break;

                        List<Operation> postOperations = subContext.getPostOperations();
                        for (Operation postOperation : postOperations)
                        {
                            postOperation.perform(event, subContext);
                        }

                        if (event.getFlow().isHandled())
                            break;
                    }
                    else
                    {
                        for (RuleLifecycleListener listener : listeners)
                        {
                            listener.afterRuleConditionEvaluation(event, subContext, rule, false);
                        }
                    }
                }
                finally
                {
                    boolean autocommit = true;
                    if (ruleContext != null && ruleContext.containsKey(RuleMetadata.AUTO_COMMIT))
                    {
                        autocommit = (Boolean) ruleContext.get(RuleMetadata.AUTO_COMMIT);
                    }
                    if (autocommit)
                    {
                        event.getGraphContext().getGraph().getBaseGraph().commit();
                    }
                    
                    event.selectionPop();

                    long ruleTimeCompleted = System.currentTimeMillis();
                    if (ruleContext != null)
                    {
                        int timeTaken = (int) (ruleTimeCompleted - ruleTimeStarted);
                        logTimeTakenByRuleProvider(event.getGraphContext(), ruleContext, i, timeTaken);
                    }
                }
            }
            catch (RuntimeException ex)
            {
                String exMsg = "Error encountered while evaluating rule: " + rule;
                String logMsg = exMsg + "\n" +  ex.getMessage();
                log.severe(logMsg);
                if (ruleContext != null)
                {
                    Object origin = ruleContext.get(RuleMetadata.ORIGIN);
                    if (origin != null)
                        exMsg += "\n  From: " + origin;

                    Object location = ruleContext.get(org.ocpsoft.rewrite.config.RuleMetadata.PROVIDER_LOCATION);
                    if (location != null)
                        exMsg += "\n  Defined in: " + location;
                }
                throw new WindupException(exMsg, ex);
                
            }
        }


        for (RuleLifecycleListener listener : listeners)
        {
            listener.afterExecution();
        }
    }

    private boolean handleBindings(final Rewrite event, final EvaluationContextImpl context,
                ParameterValueStore values)
    {
        boolean result = true;
        ParameterStore store = (ParameterStore) context.get(ParameterStore.class);

        for (Entry<String, Parameter<?>> entry : store)
        {
            Parameter<?> parameter = entry.getValue();
            String value = values.retrieve(parameter);

            if (!ParameterUtils.enqueueSubmission(event, context, parameter, value))
            {
                result = false;
                break;
            }
        }
        return result;
    }

    /*
     * Getters
     */

    @Override
    public List<Operation> getOperations()
    {
        return Collections.emptyList();
    }

    private static class EvaluationContextImpl extends ContextBase implements EvaluationContext
    {
        private final List<Operation> preOperations = new ArrayList<>();
        private final List<Operation> postOperations = new ArrayList<>();
        private RewriteState state;

        public EvaluationContextImpl()
        {
            put(ParameterStore.class, new DefaultParameterStore());
        }

        @Override
        public void addPreOperation(final Operation operation)
        {
            this.preOperations.add(operation);
        }

        @Override
        public void addPostOperation(final Operation operation)
        {
            this.preOperations.add(operation);
        }

        /**
         * Get an immutable view of the added pre-{@link Operation} instances.
         */
        public List<Operation> getPreOperations()
        {
            return Collections.unmodifiableList(preOperations);
        }

        /**
         * Get an immutable view of the added post-{@link Operation} instances.
         */
        public List<Operation> getPostOperations()
        {
            return Collections.unmodifiableList(postOperations);
        }

        @Override
        public String toString()
        {
            return "EvaluationContextImpl [preOperations=" + preOperations + ", postOperations=" + postOperations + "]";
        }

        /**
         * Clears the state of this context so that it may be reused, saving instantiation cost during rule iteration.
         */
        public void clear()
        {
            this.postOperations.clear();
            this.postOperations.clear();
        }

        @Override
        public RewriteState getState()
        {
            return state;
        }

        public void setState(RewriteState state)
        {
            this.state = state;
        }
    }

    @Override
    public Set<String> getRequiredParameterNames()
    {
        return Collections.emptySet();
    }

    @Override
    public void setParameterStore(final ParameterStore parent)
    {
        for (int i = 0; i < config.getRules().size(); i++)
        {
            Rule rule = config.getRules().get(i);

            if (!(rule instanceof RuleBuilder))
                continue;

            ParameterizedCallback callback = new ParameterizedCallbackImpl(rule, parent);

            Visitor<Condition> conditionVisitor = new ParameterizedConditionVisitor(callback);
            new ConditionVisit(rule).accept(conditionVisitor);

            Visitor<Operation> operationVisitor = new ParameterizedOperationVisitor(callback);
            new OperationVisit(rule).accept(operationVisitor);
        }
    }

    /**
     *
     */
    private static class ParameterizedCallbackImpl implements ParameterizedCallback
    {

        private final Rule rule;
        private final ParameterStore parent;

        public ParameterizedCallbackImpl(Rule rule, ParameterStore parent)
        {
            this.rule = rule;
            this.parent = parent;
        }

        @Override
        public void call(Parameterized parameterized)
        {
            Set<String> names = parameterized.getRequiredParameterNames();
            if (!(rule instanceof RuleBuilder))
                return;

            ParameterStore store = ((RuleBuilder) rule).getParameterStore();

            for (Entry<String, Parameter<?>> entry : parent)
            {
                String name = entry.getKey();
                Parameter<?> parentParam = entry.getValue();

                if (!store.contains(name))
                {
                    store.get(name, parentParam);
                    continue;
                }

                Parameter<?> parameter = store.get(name);
                for (Binding binding : parameter.getBindings())
                {
                    if (!parentParam.getBindings().contains(binding))
                        throwRedefinitionError(rule, name);
                }

                for (Constraint<String> constraint : parameter.getConstraints())
                {
                    if (!parentParam.getConstraints().contains(constraint))
                        throwRedefinitionError(rule, name);
                }

                for (Transposition<String> transposition : parameter.getTranspositions())
                {
                    if (!parentParam.getTranspositions().contains(transposition))
                        throwRedefinitionError(rule, name);
                }

                if (parentParam.getConverter() != null
                            && !parentParam.getConverter().equals(parameter.getConverter()))
                    throwRedefinitionError(rule, name);

                if (parentParam.getValidator() != null
                            && !parentParam.getValidator().equals(parameter.getValidator()))
                    throwRedefinitionError(rule, name);
            }

            for (String name : names)
            {
                Parameter<?> parameter = store.get(name, new DefaultParameter(name));
                if (parameter instanceof ConfigurableParameter<?>)
                    ((ConfigurableParameter<?>) parameter).bindsTo(Evaluation.property(name));
            }
            parameterized.setParameterStore(store);

        }

        private void throwRedefinitionError(Rule rule, String name)
        {
            throw new IllegalStateException("Subset cannot re-configure parameter [" + name
                        + "] that was configured in parent Configuration. Re-definition was attempted at ["
                        + rule + "] ");
        }

    }

    /**
     * Add a {@link RuleLifecycleListener} to receive events when {@link Rule} instances are evaluated, executed, and
     * their results.
     */
    public ListenerRegistration<RuleLifecycleListener> addLifecycleListener(final RuleLifecycleListener listener)
    {
        this.listeners.add(listener);
        return new ListenerRegistration<RuleLifecycleListener>()
        {
            @Override
            public RuleLifecycleListener removeListener()
            {
                listeners.remove(listener);
                return listener;
            }
        };

    }
}
