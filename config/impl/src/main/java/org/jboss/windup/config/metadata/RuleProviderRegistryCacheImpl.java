package org.jboss.windup.config.metadata;

import org.jboss.windup.config.GraphRewrite;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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

    /**
     * This value is a hack to allow us to only print a load error to the console once, even
     * if we are called multiple times.
     *
     * Each incident will still be logged to the detailed log file.
     */
    private static boolean loadErrorPrinted = false;

    @Inject
    private GraphContextFactory graphContextFactory;
    @Inject
    private RuleLoader ruleLoader;

    private Set<Path> userRulesPaths = new LinkedHashSet<>();

    private List<TechnologyReferenceTransformer> cachedTransformers;
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
        
        Set<String> sourceTechnologies = new HashSet<>();
        for (TechnologyReference technologyReference : sourceOptions)
        {
            sourceTechnologies.add(technologyReference.getId());
        }
        return sourceTechnologies;
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

        Set<String> targetTechnologies = new HashSet<>();
        for (TechnologyReference technologyReference : targetOptions)
        {
            targetTechnologies.add(technologyReference.getId());
        }
        return targetTechnologies;
    }

    private void addTransformers(Set<TechnologyReference> techs)
    {
        Set<TechnologyReference> newTechs = new HashSet<>();
        for (TechnologyReferenceTransformer transformer : getTechnologyTransformers())
        {
            for (TechnologyReference originalTech : techs)
            {
                if (originalTech.matches(transformer.getTarget()))
                {
                    newTechs.add(transformer.getOriginal());
                }
            }
        }
        techs.addAll(newTechs);
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
    public RuleProviderRegistry getRuleProviderRegistry(GraphContext graphContext)
    {
        initCaches(graphContext);
        return this.cachedRegistry;
    }

    private List<TechnologyReferenceTransformer> getTechnologyTransformers()
    {
        return this.cachedTransformers;
    }

    private void initCaches(GraphContext graphContext)
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
        GraphRewrite event = new GraphRewrite(graphContext);
        this.cachedTransformers = TechnologyReferenceTransformer.getTransformers(event);
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
