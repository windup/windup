package org.jboss.windup.config.loader;

import java.util.List;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;

/**
 * Each configuration extension will implement this interface to provide a list of WindupRuleProviders. A
 * GraphConfigurationLoader will pull in all WindupRuleProviders and sort them based upon the provider's metadata.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface RuleProviderLoader {
    /**
     * Indicates that these are not classloader based Java rules. Instead these are rules loaded from the filesystem.
     * <p>
     * This can be used to display only XML rules to the user, in cases where they are not interested in other rules.
     */
    boolean isFileBased();

    /**
     * Return all {@link AbstractRuleProvider} instances that are relevant for this loader.
     */
    List<RuleProvider> getProviders(RuleLoaderContext ruleLoaderContext);
}
