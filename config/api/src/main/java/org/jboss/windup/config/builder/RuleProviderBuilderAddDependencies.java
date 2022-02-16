package org.jboss.windup.config.builder;

import org.jboss.windup.config.AbstractRuleProvider;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderCustom;
import org.ocpsoft.rewrite.config.Rule;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public interface RuleProviderBuilderAddDependencies
{
    /**
     * Indicates that the current ruleset should execute after the ruleset with the given id
     */
    RuleProviderBuilderAddDependencies addExecuteAfter(String id);

    /**
     * Indicates that the current ruleset should execute after the ruleset with the type
     */
    RuleProviderBuilderAddDependencies addExecuteAfter(Class<? extends AbstractRuleProvider> type);

    /**
     * Indicates that the current ruleset should execute before the ruleset with the given id
     */
    RuleProviderBuilderAddDependencies addExecuteBefore(String id);

    /**
     * Indicates that the current ruleset should execute before the ruleset with the type
     */
    RuleProviderBuilderAddDependencies addExecuteBefore(Class<? extends AbstractRuleProvider> type);

    /**
     * Begin defining a {@link Rule} instance.
     */
    ConfigurationRuleBuilderCustom addRule();

    /**
     * Begin enhancing a {@link Rule} instance based on the given {@link Rule}.
     */
    ConfigurationRuleBuilderCustom addRule(Rule rule);
}
