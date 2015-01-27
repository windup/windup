package org.jboss.windup.config.builder;

import org.jboss.windup.config.WindupRuleProvider;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderCustom;
import org.ocpsoft.rewrite.config.Rule;

/**
 * @author jsightler
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public interface WindupRuleProviderBuilderAddDependencies
{
    /**
     * Indicates that the current ruleset should execute after the ruleset with the given id
     */
    public WindupRuleProviderBuilderAddDependencies addExecuteAfter(String id);

    /**
     * Indicates that the current ruleset should execute after the ruleset with the type
     */
    public WindupRuleProviderBuilderAddDependencies addExecuteAfter(Class<? extends WindupRuleProvider> type);

    /**
     * Indicates that the current ruleset should execute before the ruleset with the given id
     */
    public WindupRuleProviderBuilderAddDependencies addExecuteBefore(String id);

    /**
     * Indicates that the current ruleset should execute before the ruleset with the type
     */
    public WindupRuleProviderBuilderAddDependencies addExecuteBefore(Class<? extends WindupRuleProvider> type);

    /**
     * Begin defining a {@link Rule} instance.
     */
    public ConfigurationRuleBuilderCustom addRule();

    /**
     * Begin enhancing a {@link Rule} instance based on the given {@link Rule}.
     */
    public ConfigurationRuleBuilderCustom addRule(Rule rule);
}
