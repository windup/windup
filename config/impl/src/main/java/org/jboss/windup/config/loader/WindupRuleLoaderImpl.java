package org.jboss.windup.config.loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.WindupRuleMetadata;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.util.ServiceLogger;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.bind.Evaluation;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionVisit;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.OperationVisit;
import org.ocpsoft.rewrite.config.ParameterizedCallback;
import org.ocpsoft.rewrite.config.ParameterizedConditionVisitor;
import org.ocpsoft.rewrite.config.ParameterizedOperationVisitor;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.config.RuleBuilder;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.param.ConfigurableParameter;
import org.ocpsoft.rewrite.param.DefaultParameter;
import org.ocpsoft.rewrite.param.Parameter;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.param.ParameterizedRule;
import org.ocpsoft.rewrite.util.Visitor;

public class WindupRuleLoaderImpl implements WindupRuleLoader
{
    public static Logger LOG = Logger.getLogger(WindupRuleLoaderImpl.class.getName());

    @Inject
    private Imported<WindupRuleProviderLoader> loaders;

    public WindupRuleLoaderImpl()
    {
    }

    @Override
    public WindupRuleMetadata loadConfiguration(GraphContext context, Predicate<WindupRuleProvider> ruleProviderFilter)
    {
        return build(context, ruleProviderFilter);
    }

    /**
     * Prints all of the {@link RulePhase} objects in the order that they should execute. This is primarily for debug purposes and should be called
     * before the entire {@link WindupRuleProvider} list is sorted, as this will allow us to print the {@link RulePhase} list without the risk of
     * user-introduced cycles making the sort impossible.
     */
    private void printRulePhases(List<WindupRuleProvider> allProviders)
    {
        List<WindupRuleProvider> unsortedPhases = new ArrayList<>();
        for (WindupRuleProvider provider : allProviders)
        {
            if (provider instanceof RulePhase)
                unsortedPhases.add(provider);
        }
        List<WindupRuleProvider> sortedPhases = WindupRuleProviderSorter.sort(unsortedPhases);
        StringBuilder rulePhaseSB = new StringBuilder();
        for (WindupRuleProvider phase : sortedPhases)
        {
            Class<?> unproxiedClass = Proxies.unwrap(phase).getClass();
            rulePhaseSB.append("Phase: ").append(unproxiedClass.getSimpleName()).append("\n");
        }
        LOG.info("Rule Phases: [\n" + rulePhaseSB.toString() + "]");
    }

    private void checkForDuplicates(List<WindupRuleProvider> providers)
    {
        // using a map so that we can easily pull out the previous value later (in the case of a duplicate)
        Map<WindupRuleProvider, WindupRuleProvider> dupeSet = new HashMap<>(providers.size());
        for (WindupRuleProvider provider : providers)
        {
            WindupRuleProvider previousProvider = dupeSet.get(provider);
            if (previousProvider != null)
            {
                String typeMessage;
                if (previousProvider.getClass().equals(provider.getClass()))
                {
                    typeMessage = " (type: " + Proxies.unwrapProxyClassName(provider.getClass()) + ")";
                }
                else
                {
                    typeMessage = " (types: " + Proxies.unwrapProxyClassName(previousProvider.getClass()) + " and "
                                + Proxies.unwrapProxyClassName(provider.getClass()) + ")";
                }

                throw new WindupException("Found two providers with the same id: " + provider.getID() + typeMessage);
            }
            dupeSet.put(provider, provider);
        }
    }

    private List<WindupRuleProvider> getProviders(GraphContext context)
    {
        List<WindupRuleProvider> unsortedProviders = new ArrayList<>();
        for (WindupRuleProviderLoader loader : loaders)
        {
            unsortedProviders.addAll(loader.getProviders(context));
        }

        checkForDuplicates(unsortedProviders);

        printRulePhases(unsortedProviders);

        List<WindupRuleProvider> sortedProviders = WindupRuleProviderSorter.sort(unsortedProviders);
        ServiceLogger.logLoadedServices(LOG, WindupRuleProvider.class, sortedProviders);

        return Collections.unmodifiableList(sortedProviders);
    }

    private WindupRuleMetadata build(GraphContext context, Predicate<WindupRuleProvider> ruleProviderFilter)
    {

        ConfigurationBuilder result = ConfigurationBuilder.begin();

        List<WindupRuleProvider> providers = getProviders(context);
        WindupRuleMetadata executionMetadata = new WindupRuleMetadata();
        executionMetadata.setProviders(providers);
        for (WindupRuleProvider provider : providers)
        {
            if (ruleProviderFilter != null)
            {
                boolean accepted = ruleProviderFilter.accept(provider);
                LOG.info((accepted ? "Accepted" : "Skipped") + ": [" + provider + "] by filter [" + ruleProviderFilter + "]");
                if (!accepted)
                    continue;
            }

            Configuration cfg = provider.getConfiguration(context);
            List<Rule> rules = cfg.getRules();
            executionMetadata.setRules(provider, rules);

            int i = 0;
            for (final Rule rule : rules)
            {
                i++;
                if (rule instanceof Context)
                    provider.enhanceMetadata((Context) rule);

                if (rule instanceof RuleBuilder && StringUtils.isBlank(rule.getId()))
                {
                    ((RuleBuilder) rule).withId(generatedRuleID(provider, rule, i));
                }

                result.addRule(rule);

                if (rule instanceof ParameterizedRule)
                {
                    ParameterizedCallback callback = new ParameterizedCallback()
                    {
                        @Override
                        public void call(Parameterized parameterized)
                        {
                            Set<String> names = parameterized.getRequiredParameterNames();
                            ParameterStore store = ((ParameterizedRule) rule).getParameterStore();

                            if (names != null)
                                for (String name : names)
                                {
                                    Parameter<?> parameter = store.get(name, new DefaultParameter(name));
                                    if (parameter instanceof ConfigurableParameter<?>)
                                        ((ConfigurableParameter<?>) parameter).bindsTo(Evaluation.property(name));
                                }

                            parameterized.setParameterStore(store);
                        }
                    };

                    Visitor<Condition> conditionVisitor = new ParameterizedConditionVisitor(callback);
                    new ConditionVisit(rule).accept(conditionVisitor);

                    Visitor<Operation> operationVisitor = new ParameterizedOperationVisitor(callback);
                    new OperationVisit(rule).accept(operationVisitor);
                }
            }
        }

        executionMetadata.setConfiguration(result);
        return executionMetadata;
    }

    private String generatedRuleID(WindupRuleProvider provider, Rule rule, int idx)
    {
        String provID = provider.getID().replace("org.jboss.windup.rules.", "w:");
        return "GeneratedID_" + provID + "_" + idx;
    }
}
