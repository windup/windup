package org.jboss.windup.config.loader;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.RuleProviderRegistry;
import org.jboss.windup.config.phase.RulePhase;
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
import org.ocpsoft.rewrite.param.ConfigurableParameter;
import org.ocpsoft.rewrite.param.DefaultParameter;
import org.ocpsoft.rewrite.param.Parameter;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterizedRule;
import org.ocpsoft.rewrite.util.Visitor;

import static org.apache.commons.lang3.StringUtils.*;

public class RuleLoaderImpl implements RuleLoader
{
    public static Logger LOG = Logger.getLogger(RuleLoaderImpl.class.getName());

    @Inject
    private Imported<RuleProviderLoader> loaders;

    public RuleLoaderImpl()
    {
    }

    @Override
    public RuleProviderRegistry loadConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return buildRegistry(ruleLoaderContext);
    }

    /**
     * Prints all of the {@link RulePhase} objects in the order that they should execute. This is primarily for debug purposes and should be called
     * before the entire {@link RuleProvider} list is sorted, as this will allow us to print the {@link RulePhase} list without the risk of
     * user-introduced cycles making the sort impossible.
     */
    private void printRulePhases(List<RuleProvider> allProviders)
    {
        List<RuleProvider> unsortedPhases = new ArrayList<>();
        for (RuleProvider provider : allProviders)
        {
            if (provider instanceof RulePhase)
                unsortedPhases.add(provider);
        }
        List<RuleProvider> sortedPhases = RuleProviderSorter.sort(unsortedPhases);
        StringBuilder rulePhaseSB = new StringBuilder();
        for (RuleProvider phase : sortedPhases)
        {
            Class<?> unproxiedClass = Proxies.unwrap(phase).getClass();
            rulePhaseSB.append("\tPhase: ").append(unproxiedClass.getSimpleName()).append(System.lineSeparator());
        }
        LOG.info("Rule Phases: [\n" + rulePhaseSB.toString() + "]");
    }

    private void checkForDuplicateProviders(List<RuleProvider> providers)
    {
        /*
         * We are using a map so that we can easily pull out the previous value later (in the case of a duplicate)
         */
        Map<RuleProvider, RuleProvider> duplicates = new HashMap<>(providers.size());
        for (RuleProvider provider : providers)
        {
            RuleProvider previousProvider = duplicates.get(provider);
            if (previousProvider != null)
            {
                String typeMessage;
                String currentProviderOrigin = provider.getMetadata().getOrigin();
                String previousProviderOrigin = previousProvider.getMetadata().getOrigin();
                if (previousProvider.getClass().equals(provider.getClass()))
                {
                    typeMessage = " (type: " + previousProviderOrigin + " and " + currentProviderOrigin + ")";
                }
                else
                {
                    typeMessage = " (types: " + Proxies.unwrapProxyClassName(previousProvider.getClass()) + " at " + previousProviderOrigin
                                + " and " + Proxies.unwrapProxyClassName(provider.getClass()) + " at " + currentProviderOrigin + ")";
                }

                throw new WindupException("Found two providers with the same id: " + provider.getMetadata().getID() + typeMessage);
            }
            duplicates.put(provider, provider);
        }
    }

    private List<RuleProvider> loadProviders(RuleLoaderContext ruleLoaderContext)
    {
        LOG.info("Starting provider load...");

        List<RuleProvider> unsortedProviders = new ArrayList<>();
        StreamSupport.stream(loaders.spliterator(), false)
                .filter((loader -> !(ruleLoaderContext.isFileBasedRulesOnly() && !loader.isFileBased())))
                .forEach(loader -> unsortedProviders.addAll(loader.getProviders(ruleLoaderContext)));

        LOG.info("Loaded, now sorting, etc");

        checkForDuplicateProviders(unsortedProviders);

        printRulePhases(unsortedProviders);

        List<RuleProvider> sortedProviders = RuleProviderSorter.sort(unsortedProviders);
        ServiceLogger.logLoadedServices(LOG, RuleProvider.class, sortedProviders);

        LOG.info("Finished provider load");
        return Collections.unmodifiableList(sortedProviders);
    }

    private RuleProviderRegistry buildRegistry(RuleLoaderContext ruleLoaderContext)
    {
        List<Rule> allRules = new ArrayList<>(2000); // estimate of how many rules we will likely see TODO: careful with this

        List<RuleProvider> providers = loadProviders(ruleLoaderContext);
        RuleProviderRegistry registry = new RuleProviderRegistry();
        registry.setProviders(providers);

        // Get override rules from override providers (if any)
        Map<RuleKey, Rule> overrideRules = new HashMap<>();
        providers.stream()
                .filter(provider -> provider.getMetadata().isOverrideProvider())
                .forEach(provider -> {
                    provider.getConfiguration(null).getRules().forEach(rule -> {
                        RuleKey ruleKey = new RuleKey(provider.getMetadata().getID(), rule.getId());
                        overrideRules.put(ruleKey, rule);
                    });
                });

        // Add provider->rules mappings to the registry and, for each rule, inject parameters if applicable
        for (RuleProvider provider : providers)
        {
            if (ruleLoaderContext.getRuleProviderFilter() != null)
            {
                boolean accepted = ruleLoaderContext.getRuleProviderFilter().accept(provider);
                LOG.info((accepted ? "Accepted" : "Skipped") + ": [" + provider + "] by filter [" + ruleLoaderContext.getRuleProviderFilter() + "]");
                if (!accepted)
                    continue;
            }

            // these are not used directly... they only override others
            if (provider.getMetadata().isOverrideProvider())
                continue;

            Configuration cfg = provider.getConfiguration(ruleLoaderContext);
            List<Rule> rules = replaceOverridingRules(cfg, overrideRules, provider);
            registry.addRulesForProvider(provider, rules);

            for (int i = 0; i < rules.size(); i++) {
                Rule rule = rules.get(i);

                AbstractRuleProvider.enhanceRuleMetadata(provider, rule);

                if (rule instanceof RuleBuilder && isBlank(rule.getId()))
                {
                    ((RuleBuilder) rule).withId(generatedRuleID(provider, i + 1));
                }

                allRules.add(rule);

                if (rule instanceof ParameterizedRule) {
                    injectParametersIntoRule(rule);
                }
            }
        }

        ConfigurationBuilder registryConfiguration = ConfigurationBuilder.begin();
        allRules.forEach(rule -> registryConfiguration.addRule());
        registry.setConfiguration(registryConfiguration);
        
        return registry;
    }

    /**
     * Inject this rule's required parameters
     */
    private static void injectParametersIntoRule(Rule rule) {
        ParameterizedCallback callback = parameterized -> {
            Set<String> params = parameterized.getRequiredParameterNames();
            ParameterStore store = ((ParameterizedRule) rule).getParameterStore();

            if (params != null)
                for (String param : params)
                {
                    Parameter<?> parameter = store.get(param, new DefaultParameter(param));
                    if (parameter instanceof ConfigurableParameter<?>)
                        ((ConfigurableParameter<?>) parameter).bindsTo(Evaluation.property(param));
                }

            parameterized.setParameterStore(store);
        };

        Visitor<Condition> conditionVisitor = new ParameterizedConditionVisitor(callback);
        new ConditionVisit(rule).accept(conditionVisitor);

        Visitor<Operation> operationVisitor = new ParameterizedOperationVisitor(callback);
        new OperationVisit(rule).accept(operationVisitor);
    }

    /**
     * Replace rules with overriding rules if present for a given provider
     */
    private List<Rule> replaceOverridingRules(Configuration cfg, Map<RuleKey, Rule> overrideRules, RuleProvider provider) {
        List<Rule> rules = new ArrayList<>(cfg.getRules());
        ListIterator<Rule> ruleIterator = rules.listIterator();
        while (ruleIterator.hasNext())
        {
            Rule rule = ruleIterator.next();
            Rule overrideRule = overrideRules.get(new RuleKey(provider.getMetadata().getID(), rule.getId()));
            Optional.ofNullable(overrideRule)
                    .ifPresent(r -> {
                        LOG.info("Replacing rule " + rule.getId() + " with a user override!");
                        ruleIterator.set(r);
                    });
        }
        return rules;
    }

    private String generatedRuleID(RuleProvider provider, int idx)
    {
        return String.format("%s_%s", provider.getMetadata().getID(), idx);
    }
}
