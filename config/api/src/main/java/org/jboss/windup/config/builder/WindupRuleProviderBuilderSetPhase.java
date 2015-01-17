package org.jboss.windup.config.builder;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.phase.RulePhase;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;

/**
 * @author jsightler
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface WindupRuleProviderBuilderSetPhase
{
    /**
     * Set the {@link RulePhase} for this {@link WindupRuleProvider}
     */
    WindupRuleProviderBuilderAddDependencies setPhase(Class<? extends RulePhase> phase);

    /**
     * Set a {@link Predicate} to supplement metadata for each {@link Rule} provided by this builder.
     */
    WindupRuleProviderBuilderMetadataSetPhase setMetadataEnhancer(Predicate<Context> enhancer);
}
