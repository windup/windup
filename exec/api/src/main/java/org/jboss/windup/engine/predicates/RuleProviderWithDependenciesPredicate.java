package org.jboss.windup.engine.predicates;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;

/**
 * Executes only the given rule with all it's dependencies and pre-phases.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class RuleProviderWithDependenciesPredicate implements Predicate<RuleProvider> {
    private static final Logger LOG = Logger.getLogger(RuleProviderWithDependenciesPredicate.class.getName());

    private final List<RuleProvider> ruleProviders;

    @SafeVarargs
    public RuleProviderWithDependenciesPredicate(Class<? extends RuleProvider> provider, Class<? extends RuleProvider>... providers)
            throws InstantiationException, IllegalAccessException {
        /*
         * FIXME This assumes that the providers can be instantiated with the default constructor. It should really request an instance from Furnace.
         */
        ruleProviders = new ArrayList<>();
        ruleProviders.add(provider.newInstance());
        for (Class<? extends RuleProvider> clazz : providers) {
            ruleProviders.add(clazz.newInstance());
        }
    }

    @Override
    public boolean accept(RuleProvider provider) {
        if (!(provider instanceof AbstractRuleProvider))
            return false;

        // FIXME This execution index API needs to be exposed publicly or handled via another pattern.
        int examinedProviderExecutionIndex = ((AbstractRuleProvider) provider).getExecutionIndex();

        for (RuleProvider currentRuleProvider : this.ruleProviders) {
            // Is it before the examined one?
            int otherExecutionIndex = ((AbstractRuleProvider) currentRuleProvider).getExecutionIndex();
            if (otherExecutionIndex <= examinedProviderExecutionIndex) {
                LOG.fine("Accepting provider '" + provider.getMetadata().getID() + "' because it's before: " + currentRuleProvider.getMetadata().getID());
                return true;
            }

            List<Class<? extends RuleProvider>> executeAfter = currentRuleProvider.getMetadata().getExecuteAfter();
            List<String> executeAfterIDs = currentRuleProvider.getMetadata().getExecuteAfterIDs();
            if ((executeAfter.contains(provider.getClass())) || executeAfterIDs.contains(provider.getMetadata().getID())) {
                LOG.fine("Accepting provider: " + provider.getMetadata().getID());
                return true;
            }
            for (Class<? extends RuleProvider> afterType : executeAfter) {
                if (afterType.isAssignableFrom(provider.getClass())) {
                    LOG.fine("Accepting provider: " + provider.getMetadata().getID());
                    return true;
                }
            }
            if (currentRuleProvider.getClass().isAssignableFrom(provider.getClass())) {
                LOG.fine("Accepting provider: " + provider.getMetadata().getID());
                return true;
            }
        }

        LOG.fine("Skipping provider: " + provider.getMetadata().getID());
        return false;
    }

}