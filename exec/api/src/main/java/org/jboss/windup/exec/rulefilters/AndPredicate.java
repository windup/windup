package org.jboss.windup.exec.rulefilters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.RuleProvider;

/**
 * AND predicate which needs all of the given predicates to accept.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class AndPredicate implements Predicate<RuleProvider> {
    protected final Set<Predicate<RuleProvider>> predicates;

    /**
     * Creates the {@link AndPredicate} that returns true only if all of the given conditions are met.
     */
    @SafeVarargs
    public AndPredicate(Predicate<RuleProvider>... predicates) {
        this.predicates = new HashSet<>(Arrays.asList(predicates));
    }

    @Override
    public boolean accept(RuleProvider provider) {
        boolean result = true;
        if (this.predicates.isEmpty())
            return false;

        for (Predicate<RuleProvider> predicate : this.predicates) {
            if (!predicate.accept(provider))
                return false;
        }
        return result;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AndPredicate{ ");
        for (Predicate<RuleProvider> predicate : predicates) {
            sb.append("\t").append(predicate).append(System.lineSeparator());
        }
        return sb.append("}").toString();
    }

}
