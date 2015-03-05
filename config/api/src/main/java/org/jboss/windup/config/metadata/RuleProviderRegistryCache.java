package org.jboss.windup.config.metadata;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.windup.config.loader.RuleLoader;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;

/**
 * This class provides convenience methods for retrieving a {@link RuleProviderRegistry}. The registry is cached for a period of time in order to
 * improve performance. This is useful for cases in which you need to repeatedly query the registry for information, such as when looking up metadata
 * in the user interface.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">jsightler</a>
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
