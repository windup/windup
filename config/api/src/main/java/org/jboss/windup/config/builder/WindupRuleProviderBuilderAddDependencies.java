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
     * Add a dependency on the {@link WindupRuleProvider} with the given ID.
     */
    public WindupRuleProviderBuilderAddDependencies addDependency(String id);

    /**
     * Add a dependency on the {@link WindupRuleProvider} of the given type.
     */
    public WindupRuleProviderBuilderAddDependencies addDependency(Class<? extends WindupRuleProvider> type);

    /**
     * Begin defining {@link Rule} instances.
     */
    public ConfigurationRuleBuilderCustom addRule();
}
