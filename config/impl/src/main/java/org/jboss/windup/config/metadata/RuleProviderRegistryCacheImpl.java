package org.jboss.windup.config.metadata;

import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoader;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.util.PathUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@Singleton
public class RuleProviderRegistryCacheImpl implements RuleProviderRegistryCache
{
    private static final Logger LOG = Logger.getLogger(RuleProviderRegistryCache.class.getSimpleName());
    private static final long MAX_CACHE_AGE = 1000L * 60L * 1L;

    @Inject
    private GraphContextFactory graphContextFactory;
    @Inject
    private RuleLoader ruleLoader;

    private Set<Path> userRulesPaths = new LinkedHashSet<>();

    private RuleProviderRegistry cachedRegistry;
    private long cacheRefreshTime;

    @Override
    public void addUserRulesPath(Path path)
    {
        userRulesPaths.add(path);
    }

    @Override
    public Set<String> getAvailableTags()
    {
        Set<String> tags = new HashSet<>();
        for (RuleProvider provider : getRuleProviderRegistry().getProviders())
        {
            tags.addAll(provider.getMetadata().getTags());
        }
        return tags;
    }

    @Override
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

    @Override
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
                try (GraphContext graphContext = graphContextFactory.create())
                {
                    getRuleProviderRegistry(graphContext);
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

    @Override
    public RuleProviderRegistry getRuleProviderRegistry(GraphContext graphContext)
    {
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(graphContext);
        FileModel windupRulesPath = new FileService(graphContext).createByFilePath(PathUtil.getWindupRulesDir().toString());
        FileModel userRulesPath = new FileService(graphContext).createByFilePath(PathUtil.getUserRulesDir().toString());

        boolean pathAlreadyAdded = false;
        for (FileModel existingRulePath : configurationModel.getUserRulesPaths())
        {
            if (existingRulePath.equals(windupRulesPath))
                pathAlreadyAdded = true;
        }

        if (!pathAlreadyAdded)
        {
            configurationModel.addUserRulesPath(windupRulesPath);
            configurationModel.addUserRulesPath(userRulesPath);
        }
        for (Path additionalUserRulesPath : this.userRulesPaths)
        {
            FileModel additionalRulesFileModel = new FileService(graphContext).createByFilePath(additionalUserRulesPath.toString());
            configurationModel.addUserRulesPath(additionalRulesFileModel);
        }

        this.cachedRegistry = ruleLoader.loadConfiguration(graphContext, null);
        this.cacheRefreshTime = System.currentTimeMillis();
        return this.cachedRegistry;
    }

    private boolean cacheValid()
    {
        if (cachedRegistry == null)
            return false;

        long cacheAge = System.currentTimeMillis() - this.cacheRefreshTime;
        return cacheAge < MAX_CACHE_AGE;
    }
}
