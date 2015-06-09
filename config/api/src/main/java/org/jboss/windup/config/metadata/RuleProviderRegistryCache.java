package org.jboss.windup.config.metadata;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoader;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;

/**
 * This class provides convenience methods for retrieving a {@link RuleProviderRegistry}. The registry is cached for a period of time in order to
 * improve performance. This is useful for cases in which you need to repeatedly query the registry for information, such as when looking up metadata
 * in the user interface.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@Singleton
public class RuleProviderRegistryCache
{
    private static Logger LOG = Logger.getLogger(RuleProviderRegistryCache.class.getSimpleName());
    private static long MAX_CACHE_AGE = 1000L * 60L * 1L;

    @Inject
    private GraphContextFactory graphContextFactory;
    @Inject
    private RuleLoader ruleLoader;

    private RuleProviderRegistry cachedRegistry;
    private long cacheRefreshTime;

    /**
     * Returns all tags known by Windup.
     */
    public Set<String> getAvailableTags()
    {
        Set<String> tags = new HashSet<>();
        for (RuleProvider provider : getRuleProviderRegistry().getProviders())
        {
            tags.addAll(provider.getMetadata().getTags());
        }
        return tags;
    }

    /**
     * Retrieves a list of available source technologies from the {@link RuleProvider}s.
     */
    public Set<String> getAvailableSourceTechnologies()
    {
        Set<String> sourceOptions = new HashSet<>();
        for (RuleProvider provider : getRuleProviderRegistry().getProviders())
        {
            for (TechnologyReference technologyReference : provider.getMetadata().getSourceTechnologies())
            {
                sourceOptions.add(technologyReference.getId());
            }
        }
        return sourceOptions;
    }

    /**
     * Retrieves a list of available target technologies from the {@link RuleProvider}s.
     */
    public Set<String> getAvailableTargetTechnologies()
    {
        Set<String> targetOptions = new HashSet<>();
        for (RuleProvider provider : getRuleProviderRegistry().getProviders())
        {
            for (TechnologyReference technologyReference : provider.getMetadata().getTargetTechnologies())
            {
                targetOptions.add(technologyReference.getId());
            }
        }
        return targetOptions;
    }

    /**
     * Returns the {@link RuleProviderRegistry}, loading it if necessary.
     */
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
                try (GraphContext graphContext = graphContextFactory.create())
                {
                    this.cachedRegistry = ruleLoader.loadConfiguration(graphContext, null);
                    this.cacheRefreshTime = System.currentTimeMillis();
                    graphContext.clear();
                }
            }
            catch (Exception e)
            {
                LOG.log(Level.WARNING, "Failed to load rule information due to: " + e.getMessage(), e);
            }
            return cachedRegistry;
        }
    }

    private boolean cacheValid()
    {
        if (cachedRegistry == null)
            return false;

        long cacheAge = System.currentTimeMillis() - this.cacheRefreshTime;
        return cacheAge < MAX_CACHE_AGE;
    }
}
