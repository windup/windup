package org.jboss.windup.config.metadata;

import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.graph.GraphContext;

import java.nio.file.Path;
import java.util.Set;

/**
 * This class provides convenience methods for retrieving a {@link RuleProviderRegistry}. The registry is cached for a period of time in order to
 * improve performance. This is useful for cases in which you need to repeatedly query the registry for information, such as when looking up metadata
 * in the user interface.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface RuleProviderRegistryCache {
    /**
     * Specify that the user has added an additional path for user defined rules.
     */
    void addUserRulesPath(Path path);

    /**
     * Returns all tags known by Windup.
     */
    Set<String> getAvailableTags();

    /**
     * Retrieves a list of available source technologies from the {@link RuleProvider}s.
     */
    Set<String> getAvailableSourceTechnologies();

    /**
     * Retrieves a list of available target technologies from the {@link RuleProvider}s.
     */
    Set<String> getAvailableTargetTechnologies();

    /**
     * Returns the {@link RuleProviderRegistry}, loading it if necessary.
     */
    RuleProviderRegistry getRuleProviderRegistry();

    /**
     * Load the registry using the given {@link GraphContext}.
     */
    RuleProviderRegistry getRuleProviderRegistry(RuleLoaderContext ruleLoaderContext);
}
