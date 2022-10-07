package org.jboss.windup.exec.rulefilters;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.RuleProvider;

/**
 * NOT predicate which negates the result of given predicate..
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class NotPredicate implements Predicate<RuleProvider> {
    protected final Predicate<RuleProvider> predicate;

    /**
     * Creates an instance of {@link NotPredicate} with the given condition.
     */
    public NotPredicate(Predicate<RuleProvider> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean accept(RuleProvider provider) {
        return !this.predicate.accept(provider);
    }
}
