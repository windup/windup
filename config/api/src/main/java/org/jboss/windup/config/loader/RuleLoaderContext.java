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
    private boolean fileBasedRulesOnly;

    public RuleLoaderContext()
    {
        this.context = new ContextBase()
        {
        };
        this.rulePaths = Collections.emptyList();
        this.ruleProviderFilter = (provider) -> true;
    }

    public RuleLoaderContext(Iterable<Path> rulePaths, Predicate<RuleProvider> ruleProviderFilter)
    {
        this.context = new ContextBase()
        {
        };
        this.rulePaths = rulePaths;
        this.ruleProviderFilter = ruleProviderFilter;
    }

    public RuleLoaderContext(Context context, Iterable<Path> rulePaths, Predicate<RuleProvider> ruleProviderFilter)
    {
        this.context = context;
        this.rulePaths = rulePaths;
        this.ruleProviderFilter = ruleProviderFilter;
    }

    public RuleLoaderContext setFileBasedRulesOnly()
    {
        this.fileBasedRulesOnly = true;
        return this;
    }

    public boolean isFileBasedRulesOnly()
    {
        return fileBasedRulesOnly;
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
