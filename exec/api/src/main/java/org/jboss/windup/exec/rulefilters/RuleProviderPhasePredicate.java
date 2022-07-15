package org.jboss.windup.exec.rulefilters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.RulePhase;

/**
 * Filters the rules with given phases.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class RuleProviderPhasePredicate implements Predicate<RuleProvider> {
    private final Set<Class<? extends RulePhase>> phases;

    /**
     * Creates the {@link RuleProviderPhasePredicate} with the given phase types.
     */
    @SafeVarargs
    public RuleProviderPhasePredicate(Class<? extends RulePhase>... phases) {
        this.phases = new HashSet<>(Arrays.asList(phases));
    }

    @Override
    public boolean accept(RuleProvider provider) {
        return this.phases.contains(provider.getMetadata().getPhase());
    }
}
