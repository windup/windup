package org.jboss.windup.config.loader;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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

public class RuleLoaderImpl implements RuleLoader {
    public static Logger LOG = Logger.getLogger(RuleLoaderImpl.class.getName());

    @Inject
    private Imported<RuleProviderLoader> loaders;

    public RuleLoaderImpl() {
    }

    @Override
    public RuleProviderRegistry loadConfiguration(RuleLoaderContext ruleLoaderContext) {
        return buildRegistry(ruleLoaderContext);
    }

    private RuleProviderRegistry buildRegistry(RuleLoaderContext ruleLoaderContext) {
        List<Rule> allRules = new ArrayList<>(2000); // estimate of how many rules we will likely see

        List<RuleProvider> providers = loadProviders(ruleLoaderContext);
        RuleProviderRegistry registry = new RuleProviderRegistry();
        registry.setProviders(providers);

        // Get override rules from override providers (if any)
        Map<RuleKey, OverrideRule> overrideRules = extractOverrideRules(providers, ruleLoaderContext);

        // Add provider->rules mappings to the registry and, for each rule, inject parameters if applicable
        for (RuleProvider provider : providers) {
            if (ruleLoaderContext.getRuleProviderFilter() != null) {
                boolean accepted = ruleLoaderContext.getRuleProviderFilter().accept(provider);
                LOG.info((accepted ? "Accepted" : "Skipped") + ": [" + provider + "] by filter [" + ruleLoaderContext.getRuleProviderFilter() + "]");
                if (!accepted)
                    continue;
            }

            // https://issues.redhat.com/browse/WINDUP-3928
            // now it's time to add the overriding ruleset which didn't override any previously found rule
            // but that are "in scope" (i.e. ruleset's sources and targets fit the analysis one) for the
            // current analysis and, as such, added as "regular" ruleset.
            // The rulesets are loaded and sorted from loadProviders(ruleLoaderContext) and the override
            // tag creates a before-after edge between the original and the overriding rulesets
            if (provider.getMetadata().isOverrideProvider()) {
                final List<Rule> unusedOverrideRules = provider
                        .getConfiguration(null)
                        .getRules()
                        .stream()
                        .filter(rule -> {
                            final OverrideRule overrideRule = overrideRules
                                    .get(new RuleKey(provider.getMetadata().getID(), rule.getId()));
                            if (overrideRule != null)
                                // if the override rule hasn't been used yet,
                                // then it must the added to the rules to be executed
                                return !overrideRule.isUsed();
                            return false;
                        })
                        .collect(Collectors.toList());
                registry.addRulesForProvider(provider, unusedOverrideRules);
                enhanceRules(allRules, provider, unusedOverrideRules);
                continue;
            }

            Configuration cfg = provider.getConfiguration(ruleLoaderContext);
            List<Rule> rules = overrideRules(cfg, overrideRules, provider);
            registry.addRulesForProvider(provider, rules);

            enhanceRules(allRules, provider, rules);
        }

        ConfigurationBuilder result = ConfigurationBuilder.begin();
        for (Rule rule : allRules) {
            result.addRule(rule);
        }

        registry.setConfiguration(result);
        return registry;
    }

    private void enhanceRules(List<Rule> allRules, RuleProvider provider, List<Rule> rules) {
        for (int i = 0; i < rules.size(); i++) {
            Rule rule = rules.get(i);

            AbstractRuleProvider.enhanceRuleMetadata(provider, rule);

            if (rule instanceof RuleBuilder && isBlank(rule.getId())) {
                ((RuleBuilder) rule).withId(generatedRuleID(provider, i + 1));
            }

            allRules.add(rule);

            if (rule instanceof ParameterizedRule) {
                injectParametersIntoRule(rule);
            }
        }
    }


    private List<RuleProvider> loadProviders(RuleLoaderContext ruleLoaderContext) {
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

    private void checkForDuplicateProviders(List<RuleProvider> providers) {
        /*
         * We are using a map so that we can easily pull out the previous value later (in the case of a duplicate)
         */
        Map<RuleProvider, RuleProvider> duplicates = new HashMap<>(providers.size());
        for (RuleProvider provider : providers) {
            RuleProvider previousProvider = duplicates.get(provider);
            if (previousProvider != null) {
                String typeMessage;
                String currentProviderOrigin = provider.getMetadata().getOrigin();
                String previousProviderOrigin = previousProvider.getMetadata().getOrigin();
                if (previousProvider.getClass().equals(provider.getClass())) {
                    typeMessage = " (type: " + previousProviderOrigin + " and " + currentProviderOrigin + ")";
                } else {
                    typeMessage = " (types: " + Proxies.unwrapProxyClassName(previousProvider.getClass()) + " at " + previousProviderOrigin
                            + " and " + Proxies.unwrapProxyClassName(provider.getClass()) + " at " + currentProviderOrigin + ")";
                }

                throw new WindupException("Found two providers with the same id: " + provider.getMetadata().getID() + typeMessage);
            }
            duplicates.put(provider, provider);
        }
    }

    /**
     * Prints all of the {@link RulePhase} objects in the order that they should execute. This is primarily for debug purposes and should be called
     * before the entire {@link RuleProvider} list is sorted, as this will allow us to print the {@link RulePhase} list without the risk of
     * user-introduced cycles making the sort impossible.
     */
    private void printRulePhases(List<RuleProvider> allProviders) {
        List<RuleProvider> unsortedPhases = new ArrayList<>();
        for (RuleProvider provider : allProviders) {
            if (provider instanceof RulePhase)
                unsortedPhases.add(provider);
        }
        List<RuleProvider> sortedPhases = RuleProviderSorter.sort(unsortedPhases);
        StringBuilder rulePhaseSB = new StringBuilder();
        for (RuleProvider phase : sortedPhases) {
            Class<?> unproxiedClass = Proxies.unwrap(phase).getClass();
            rulePhaseSB.append("\tPhase: ").append(unproxiedClass.getSimpleName()).append(System.lineSeparator());
        }
        LOG.info("Rule Phases: [\n" + rulePhaseSB.toString() + "]");
    }

    private Map<RuleKey, OverrideRule> extractOverrideRules(List<RuleProvider> providers, RuleLoaderContext ruleLoaderContext) {
        Map<RuleKey, OverrideRule> overrideRules = new HashMap<>();
        providers.stream()
                .filter(provider -> provider.getMetadata().isOverrideProvider())
                .filter(provider -> ruleLoaderContext.getRuleProviderFilter() == null || ruleLoaderContext.getRuleProviderFilter().accept(provider))
                .forEach(provider -> {
                    provider.getConfiguration(null).getRules().forEach(rule -> {
                        RuleKey ruleKey = new RuleKey(provider.getMetadata().getID(), rule.getId());
                        overrideRules.put(ruleKey, new OverrideRule(rule));
                    });
                });
        return overrideRules;
    }

    /**
     * Given a set of overriding rules, replaces the original rules with the overriding ones if applicable.
     */
    private List<Rule> overrideRules(Configuration cfg, Map<RuleKey, OverrideRule> overrideRules, RuleProvider provider) {
        List<Rule> rules = new ArrayList<>(cfg.getRules());
        ListIterator<Rule> ruleIterator = rules.listIterator();
        while (ruleIterator.hasNext()) {
            Rule rule = ruleIterator.next();
            OverrideRule overrideRule = overrideRules.get(new RuleKey(provider.getMetadata().getID(), rule.getId()));
            Optional.ofNullable(overrideRule)
                    .ifPresent(r -> {
                        LOG.info("Replacing rule " + rule.getId() + " with a user override!");
                        ruleIterator.set(r.getRule());
                        r.setUsed(true);
                    });
        }
        return rules;
    }

    private void injectParametersIntoRule(Rule rule) {
        ParameterizedCallback callback = parameterized -> {
            Set<String> names = parameterized.getRequiredParameterNames();
            ParameterStore store = ((ParameterizedRule) rule).getParameterStore();

            if (names != null)
                for (String name : names) {
                    Parameter<?> parameter = store.get(name, new DefaultParameter(name));
                    if (parameter instanceof ConfigurableParameter<?>)
                        ((ConfigurableParameter<?>) parameter).bindsTo(Evaluation.property(name));
                }

            parameterized.setParameterStore(store);
        };

        Visitor<Condition> conditionVisitor = new ParameterizedConditionVisitor(callback);
        new ConditionVisit(rule).accept(conditionVisitor);

        Visitor<Operation> operationVisitor = new ParameterizedOperationVisitor(callback);
        new OperationVisit(rule).accept(operationVisitor);
    }

    private String generatedRuleID(RuleProvider provider, int idx) {
        return String.format("%s_%s", provider.getMetadata().getID(), idx);
    }
}
