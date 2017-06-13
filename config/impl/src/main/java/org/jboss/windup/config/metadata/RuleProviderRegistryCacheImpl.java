package org.jboss.windup.config.metadata;

import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoader;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.util.PathUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@Singleton
public class RuleProviderRegistryCacheImpl implements RuleProviderRegistryCache
{
    private static final Logger LOG = Logger.getLogger(RuleProviderRegistryCache.class.getName());
    private static final long MAX_CACHE_AGE = 1000L * 60L * 1L;

    /**
     * This value is a hack to allow us to only print a load error to the console once, even
     * if we are called multiple times.
     *
     * Each incident will still be logged to the detailed log file.
     */
    private static boolean loadErrorPrinted = false;

    @Inject
    private RuleLoader ruleLoader;

    private Set<Path> userRulesPaths = new LinkedHashSet<>();

    private List<TechnologyReferenceTransformer> cachedTransformers;
    private RuleProviderRegistry cachedRegistry;
    private long cacheRefreshTime;

    @Override
    public void addUserRulesPath(Path path)
    {
        this.cachedRegistry = null;
        this.cachedTransformers = null;
        userRulesPaths.add(path);
    }

    @Override
    public Set<String> getAvailableTags()
    {
        Set<String> tags = new HashSet<>();
        RuleProviderRegistry registry = getRuleProviderRegistry();
        if (registry == null)
            return Collections.emptySet();

        for (RuleProvider provider : registry.getProviders())
        {
            tags.addAll(provider.getMetadata().getTags());
        }
        return tags;
    }

    @Override
    public Set<String> getAvailableSourceTechnologies()
    {
        Set<TechnologyReference> sourceOptions = new HashSet<>();
        RuleProviderRegistry registry = getRuleProviderRegistry();
        if (registry == null)
            return Collections.emptySet();

        for (RuleProvider provider : registry.getProviders())
        {
            for (TechnologyReference technologyReference : provider.getMetadata().getSourceTechnologies())
            {
                sourceOptions.add(technologyReference);
            }
        }
        addTransformers(sourceOptions);

        return sourceOptions.stream().map(TechnologyReference::getId).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getAvailableTargetTechnologies()
    {
        Set<TechnologyReference> targetOptions = new HashSet<>();
        RuleProviderRegistry registry = getRuleProviderRegistry();
        if (registry == null)
            return Collections.emptySet();

        for (RuleProvider provider : registry.getProviders())
        {
            for (TechnologyReference technologyReference : provider.getMetadata().getTargetTechnologies())
            {
                targetOptions.add(technologyReference);
            }
        }
        addTransformers(targetOptions);

        return targetOptions.stream().map(TechnologyReference::getId).collect(Collectors.toSet());
    }

    private void addTransformers(Set<TechnologyReference> techs)
    {
        techs.addAll(getTechnologyTransformers()
                .stream()

                // Only include it if the target of the transformation will match one of the items
                //   already in the list.
                .filter(transformer -> {
                    for (TechnologyReference originalTech : techs)
                    {
                        if (originalTech.matches(transformer.getTarget()))
                        {
                            return true;
                        }
                    }
                    return false;
                })

                .map(TechnologyReferenceTransformer::getOriginal)
                .collect(Collectors.toList()));
    }

    @Override
    public RuleProviderRegistry getRuleProviderRegistry()
    {
        if (cacheValid())
        {
            return cachedRegistry;
        }
        else
        {
            this.cachedRegistry = null;
            try
            {
                Set<Path> defaultRulePaths = new HashSet<>();
                defaultRulePaths.add(PathUtil.getWindupRulesDir());
                defaultRulePaths.add(PathUtil.getUserRulesDir());

                defaultRulePaths.addAll(this.userRulesPaths);


                RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(defaultRulePaths, null);
                getRuleProviderRegistry(ruleLoaderContext);
            }
            catch (Exception e)
            {
                if (!loadErrorPrinted)
                {
                    System.err.println("Failed to load rule information due to: " + e.getMessage());
                    loadErrorPrinted = true;
                }
                LOG.log(Level.WARNING, "Failed to load rule information due to: " + e.getMessage(), e);
            }
            return cachedRegistry;
        }
    }

    @Override
    public RuleProviderRegistry getRuleProviderRegistry(RuleLoaderContext ruleLoaderContext)
    {
        initCaches(ruleLoaderContext);
        return this.cachedRegistry;
    }

    private List<TechnologyReferenceTransformer> getTechnologyTransformers()
    {
        return this.cachedTransformers;
    }

    private void initCaches(RuleLoaderContext ruleLoaderContext)
    {
        this.cachedRegistry = ruleLoader.loadConfiguration(ruleLoaderContext);
        this.cachedTransformers = TechnologyReferenceTransformer.getTransformers(ruleLoaderContext);
        this.cacheRefreshTime = System.currentTimeMillis();
    }

    private boolean cacheValid()
    {
        if (cachedRegistry == null)
            return false;

        long cacheAge = System.currentTimeMillis() - this.cacheRefreshTime;
        return cacheAge < MAX_CACHE_AGE;
    }
}
