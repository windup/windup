package org.jboss.windup.config.loader;

import java.nio.file.Path;
import java.util.Collections;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.RuleProvider;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.ContextBase;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class RuleLoaderContext
{
    private final Context context;
    private final Iterable<Path> rulePaths;
    private final Predicate<RuleProvider> ruleProviderFilter;

    public RuleLoaderContext()
    {
        this.context = new ContextBase() {};
        rulePaths = Collections.emptyList();
        ruleProviderFilter = (provider) -> true;
    }

    public RuleLoaderContext(Iterable<Path> rulePaths, Predicate<RuleProvider> ruleProviderFilter)
    {
        this.context = new ContextBase() {};
        this.rulePaths = rulePaths;
        this.ruleProviderFilter = ruleProviderFilter;
    }

    public RuleLoaderContext(Context context, Iterable<Path> rulePaths, Predicate<RuleProvider> ruleProviderFilter)
    {
        this.context = context;
        this.rulePaths = rulePaths;
        this.ruleProviderFilter = ruleProviderFilter;
    }

    public Context getContext()
    {
        return context;
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
