package org.jboss.windup.config.phase;

import org.jboss.windup.config.AbstractRuleProvider;

/**
 * This phase can occur during any phase of the execution lifecycle. It's exact placement will be defined by the
 * {@link AbstractRuleProvider} itself and the values that it returns from
 * {@link AbstractRuleProvider#getExecuteAfter()} and {@link AbstractRuleProvider#getExecuteBefore()}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class DependentPhase extends RulePhase {
    public DependentPhase() {
        super(DependentPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return null;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
