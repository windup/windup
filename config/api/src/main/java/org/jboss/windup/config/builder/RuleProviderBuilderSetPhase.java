package org.jboss.windup.config.builder;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.phase.RulePhase;

/**
 * @author jsightler
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface RuleProviderBuilderSetPhase
{
    /**
     * Set the {@link RulePhase} for this {@link AbstractRuleProvider}
     */
    RuleProviderBuilderAddDependencies setPhase(Class<? extends RulePhase> phase);
}
