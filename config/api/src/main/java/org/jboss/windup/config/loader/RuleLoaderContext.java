package org.jboss.windup.config.loader;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.RuleProvider;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class RuleLoaderContext
{
    private final Iterable<Path> rulePaths;
    private final Predicate<RuleProvider> ruleProviderFilter;

    public RuleLoaderContext()
    {
        rulePaths = Collections.emptyList();
        ruleProviderFilter = (provider) -> true;
    }

    public RuleLoaderContext(Iterable<Path> rulePaths, Predicate<RuleProvider> ruleProviderFilter)
    {
        this.rulePaths = rulePaths;
        this.ruleProviderFilter = ruleProviderFilter;
    }

    public Iterable<Path> getRulePaths()
    {
        return rulePaths;
    }

    public Predicate<RuleProvider> getRuleProviderFilter()
    {
        return ruleProviderFilter;
    }
}
