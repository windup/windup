package org.jboss.windup.config.builder;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;

/**
 * @author jsightler
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface WindupRuleProviderBuilderMetadataSetPhase
{
    /**
     * Set the {@link RulePhase} for this {@link WindupRuleProvider}
     */
    WindupRuleProviderBuilderAddDependencies setPhase(RulePhase phase);
}
