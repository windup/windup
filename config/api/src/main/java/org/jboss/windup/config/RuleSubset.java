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

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.spi.ListenerRegistration;
import org.jboss.windup.config.metadata.RuleMetadataType;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupExecutionModel;
import org.jboss.windup.graph.model.performance.RulePhaseExecutionStatisticsModel;
import org.jboss.windup.graph.model.performance.RuleProviderExecutionStatisticsModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.RuleProviderExecutionStatisticsService;
import org.jboss.windup.util.Util;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.exception.WindupStopException;
import org.ocpsoft.common.util.Assert;
import org.ocpsoft.rewrite.bind.Binding;
import org.ocpsoft.rewrite.bind.Evaluation;
import org.ocpsoft.rewrite.config.CompositeOperation;
import org.ocpsoft.rewrite.config.CompositeRule;
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
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
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
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, I - zizka at seznam.cz</a>
 */
public class RuleSubset extends DefaultOperationBuilder implements CompositeOperation, Parameterized, CompositeRule
{
    private static final Logger log = Logger.getLogger(RuleSubset.class.getName());

    /**
     * This accumulates all exceptions for later usage if there is not set Halt on Exceptions
     * Useful for tests or choose your use case ;)
     */
    private Map<String, Exception> exceptions = new LinkedHashMap<>();

    /**
     * @return the exceptions
     */
    public Map<String, Exception> getExceptions()
    {
        return exceptions;
    }

    /**
     * Used for tracking the time taken by the rules within each RuleProvider. This links from a {@link AbstractRuleProvider} to the ID of a
     * {@link RuleProviderExecutionStatisticsModel}
     */
    private final IdentityHashMap<AbstractRuleProvider, Object> timeTakenByProvider = new IdentityHashMap<>();

    /**
     * Used for tracking the time taken by each phase of execution. This links from a {@link RulePhase} to the ID of a
     * {@link RulePhaseExecutionStatisticsModel}
     */
    private final Map<Class<? extends RulePhase>, Object> timeTakenByPhase = new HashMap<>();

    private final Configuration config;

    private final List<RuleLifecycleListener> listeners = new ArrayList<>();

    private boolean alwaysHaltOnFailure = false;

    private RuleSubset(Configuration config)
    {
        Assert.notNull(config, "Configuration must not be null.");
        this.config = config;
    }

    public static RuleSubset create(Configuration config)
    {
        return new RuleSubset(config);
    }

    public void setAlwaysHaltOnFailure(boolean alwaysHaltOnFailure)
    {
        this.alwaysHaltOnFailure = alwaysHaltOnFailure;
    }

    /**
     * Logs the time taken by this rule, and attaches this to the total for the RuleProvider
     */
    private void logTimeTakenByRuleProvider(GraphContext graphContext, Context context, int ruleIndex, int timeTaken)
    {
        AbstractRuleProvider ruleProvider = (AbstractRuleProvider) context.get(RuleMetadataType.RULE_PROVIDER);
        if (ruleProvider == null)
            return;

        if (!timeTakenByProvider.containsKey(ruleProvider))
        {
            RuleProviderExecutionStatisticsModel model = new RuleProviderExecutionStatisticsService(graphContext)
                        .create();
            model.setRuleIndex(ruleIndex);
            model.setRuleProviderID(ruleProvider.getMetadata().getID());
            model.setTimeTaken(timeTaken);

            timeTakenByProvider.put(ruleProvider, model.getElement().id());
        }
        else
        {
            RuleProviderExecutionStatisticsService service = new RuleProviderExecutionStatisticsService(graphContext);
            RuleProviderExecutionStatisticsModel model = service.getById(timeTakenByProvider.get(ruleProvider));
            int prevTimeTaken = model.getTimeTaken();
            model.setTimeTaken(prevTimeTaken + timeTaken);
        }
        logTimeTakenByPhase(graphContext, ruleProvider.getMetadata().getPhase(), timeTaken);
    }

    /**
     * Logs the time taken by this rule and adds this to the total time taken for this phase
     */
    private void logTimeTakenByPhase(GraphContext graphContext, Class<? extends RulePhase> phase, int timeTaken)
    {
        if (!timeTakenByPhase.containsKey(phase))
        {
            RulePhaseExecutionStatisticsModel model = new GraphService<>(graphContext,
                        RulePhaseExecutionStatisticsModel.class).create();
            model.setRulePhase(phase.toString());
            model.setTimeTaken(timeTaken);
            model.setOrderExecuted(timeTakenByPhase.size());
            timeTakenByPhase.put(phase, model.getElement().id());
        }
        else
        {
            GraphService<RulePhaseExecutionStatisticsModel> service = new GraphService<>(graphContext,
                        RulePhaseExecutionStatisticsModel.class);
            RulePhaseExecutionStatisticsModel model = service.getById(timeTakenByPhase.get(phase));
            int prevTimeTaken = model.getTimeTaken();
            model.setTimeTaken(prevTimeTaken + timeTaken);
        }
    }

    @Override
    public void perform(Rewrite rewrite, EvaluationContext context)
    {
        if (!(rewrite instanceof GraphRewrite))
            throw new IllegalArgumentException("Rewrite must be an instanceof GraphRewrite");

        /*
         * Highly optimized loop - for performance reasons. Think before you change this! (lincolnthree)
         */
        GraphRewrite windupExecutionContext = (GraphRewrite) rewrite;

        List<Rule> rules = config.getRules();

        listeners.forEach(listener -> listener.beforeExecution(windupExecutionContext));

        EvaluationContextImpl subContext = new EvaluationContextImpl();
        rulesLoop:
        for (int i = 0; i < rules.size(); i++)
        {
            Rule rule = rules.get(i);

            Context ruleContext = rule instanceof Context ? (Context) rule : null;

            long ruleTimeStarted = System.currentTimeMillis();
            try
            {
                AbstractRuleProvider ruleProvider = (AbstractRuleProvider) ruleContext.get(RuleMetadataType.RULE_PROVIDER);
                if (ruleProvider != null && ruleProvider.getMetadata() != null && ruleProvider.getMetadata().isDisabled())
                {
                    log.info("RuleProvider is disabled, skipping: " + ruleProvider.getMetadata().getID());
                    continue;
                }

                subContext = new EvaluationContextImpl();

                // Set up rule parameters
                ParameterStore parameterStore = Optional.ofNullable((ParameterStore) context.get(ParameterStore.class)).orElse(new DefaultParameterStore());
                subContext.put(ParameterStore.class, parameterStore);
                setParameterStore(parameterStore);

                ParameterValueStore values = Optional.ofNullable((ParameterValueStore) context.get(ParameterValueStore.class)).orElse(new DefaultParameterValueStore());
                subContext.put(ParameterValueStore.class, values);

                subContext.setState(RewriteState.EVALUATING);
                subContext.put(Rule.class, rule);

                Variables.instance(windupExecutionContext).push();
                try
                {
                    // Run "before rule evaluation" listeners
                    for (RuleLifecycleListener listener : listeners)
                    {
                        boolean windupStopRequested = listener.beforeRuleEvaluation(windupExecutionContext, rule, subContext);
                        if (windupStopRequested)
                        {
                            String msg = Util.WINDUP_BRAND_NAME_ACRONYM+" was requested to stop before beforeRuleEvaluation() of " + rule.getId() + ", skipping further rules.";
                            log.fine(msg);
                            windupExecutionContext.setWindupStopException(new WindupStopException(msg));
                            break rulesLoop;
                        }
                    }

                    // Check if this rule applies to the context
                    if (rule.evaluate(windupExecutionContext, subContext))
                    {
                        // Run "after rule condition evaluation" listeners
                        for (RuleLifecycleListener listener : listeners)
                        {
                            listener.afterRuleConditionEvaluation(windupExecutionContext, subContext, rule, true);
                        }

                        // Binds values to the subContext?
                        if (!handleBindings(windupExecutionContext, subContext, values))
                            continue;

                        // Start executing rule
                        subContext.setState(RewriteState.PERFORMING);
                        final Object ruleProviderDesc = ((RuleBuilder) rule).get(RuleMetadataType.RULE_PROVIDER);
                        log.info("Rule [" + ruleProviderDesc + "] matched and will be performed.");

                        // Run "before rule operations performed" listeners
                        for (RuleLifecycleListener listener : listeners)
                        {
                            boolean windupStopRequested = listener.beforeRuleOperationsPerformed(windupExecutionContext, subContext, rule);
                            if (windupStopRequested)
                            {
                                String msg = Util.WINDUP_BRAND_NAME_ACRONYM+" was requested to stop before beforeRuleOperationsPerformed() of " + rule.getId() + ", skipping further rules.";
                                log.warning(msg);
                                windupExecutionContext.setWindupStopException(new WindupStopException(msg));
                                break rulesLoop;
                            }
                        }

                        // Run preoperations if any
                        List<Operation> preOperations = subContext.getPreOperations();
                        for (Operation preOperation : preOperations)
                        {
                            preOperation.perform(windupExecutionContext, subContext);
                        }

                        rule.perform(windupExecutionContext, subContext);

                        for (RuleLifecycleListener listener : listeners)
                        {
                            listener.afterRuleOperationsPerformed(windupExecutionContext, subContext, rule);
                        }

                        List<Operation> postOperations = subContext.getPostOperations();
                        for (Operation postOperation : postOperations)
                        {
                            postOperation.perform(windupExecutionContext, subContext);
                        }
                    }
                    else
                    {
                        for (RuleLifecycleListener listener : listeners)
                        {
                            listener.afterRuleConditionEvaluation(windupExecutionContext, subContext, rule, false);
                        }
                    }
                }
                catch (WindupStopException ex)
                {
                    final String msg = Util.WINDUP_BRAND_NAME_ACRONYM+" was requested to stop during execution of " + rule.getId() + ", skipping further rules.";
                    log.fine(msg);
                    windupExecutionContext.setWindupStopException(new WindupStopException(msg, ex));
                    windupExecutionContext.getGraphContext().service(WindupExecutionModel.class).create().setStopMessage(msg);
                    break;
                }
                finally
                {
                    boolean autocommit = true;
                    if (ruleContext != null && ruleContext.containsKey(RuleMetadataType.AUTO_COMMIT))
                        autocommit = (Boolean) ruleContext.get(RuleMetadataType.AUTO_COMMIT);

                    if (autocommit)
                        windupExecutionContext.getGraphContext().commit();

                    Variables.instance(windupExecutionContext).pop();

                    long ruleTimeCompleted = System.currentTimeMillis();
                    if (ruleContext != null)
                    {
                        int timeTaken = (int) (ruleTimeCompleted - ruleTimeStarted);
                        logTimeTakenByRuleProvider(windupExecutionContext.getGraphContext(), ruleContext, i, timeTaken);
                    }
                }
            }
            catch (RuntimeException ex)
            {
                for (RuleLifecycleListener listener : listeners)
                {
                    listener.afterRuleExecutionFailed(windupExecutionContext, subContext, rule, ex);
                }
                String exMsg = "Error encountered while evaluating rule: " + rule;
                String logMsg = exMsg + System.lineSeparator() + StringUtils.defaultString(ex.getMessage(), "(Exception message is not set)");
                log.log(Level.SEVERE, logMsg, ex);
                if (ruleContext != null)
                {
                    Object origin = ruleContext.get(RuleMetadataType.ORIGIN);
                    if (origin != null)
                        exMsg += System.lineSeparator()+"  From: " + origin;

                    Object location = ruleContext.get(org.ocpsoft.rewrite.config.RuleMetadata.PROVIDER_LOCATION);
                    if (location != null)
                        exMsg += System.lineSeparator()+"  Defined in: " + location;
                }

                // Depending on RuleProvider's haltOnException, halt Windup on exception.
                AbstractRuleProvider ruleProvider = (AbstractRuleProvider) ruleContext.get(RuleMetadataType.RULE_PROVIDER);
                boolean halt = alwaysHaltOnFailure || ruleProvider.getMetadata().isHaltOnException();
                Object halt_ = ruleContext.get(RuleMetadataType.HALT_ON_EXCEPTION);
                halt |= (halt_ instanceof Boolean && ((Boolean) halt_).booleanValue());
                if (halt)
                    throw new WindupException(exMsg, ex);
                else
                    exceptions.put(rule.getId(), ex);
            }
        }

        if (windupExecutionContext.getWindupStopException() == null)
            for (RuleLifecycleListener listener : listeners)
                listener.afterExecution(windupExecutionContext);
    }

    private boolean handleBindings(final Rewrite event, final EvaluationContextImpl context, ParameterValueStore valueStore)
    {
        boolean result = true;
        ParameterStore store = (ParameterStore) context.get(ParameterStore.class);
        for (Entry<String, Parameter<?>> entry : store)
        {
            Parameter<?> parameter = entry.getValue();
            String values = valueStore.retrieve(parameter);
            if (!ParameterUtils.enqueueSubmission(event, context, parameter, values))
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
     * Add a {@link RuleLifecycleListener} to receive events when {@link Rule} instances are evaluated, executed, and their results.
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

    @Override
    public String getId()
    {
        return "RuleSubset_" + config.hashCode();
    }

    @Override
    public boolean evaluate(Rewrite event, EvaluationContext context)
    {
        return config != null && config.getRules() != null && !config.getRules().isEmpty();
    }

    @Override
    public List<Rule> getRules()
    {
        return config == null ? null : config.getRules();
    }

    @Override
    public String toString()
    {
        return "RuleSubset.create(" + this.config + ")";
    }
}
