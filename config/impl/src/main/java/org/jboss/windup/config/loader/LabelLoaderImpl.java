package org.jboss.windup.config.loader;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.LabelProvider;
import org.jboss.windup.config.metadata.LabelProviderRegistry;
import org.jboss.windup.util.ServiceLogger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class LabelLoaderImpl implements LabelLoader
{
    public static Logger LOG = Logger.getLogger(LabelLoaderImpl.class.getName());

    @Inject
    private Imported<LabelProviderLoader> loaders;

    public LabelLoaderImpl()
    {
    }

    @Override
    public LabelProviderRegistry loadConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return build(ruleLoaderContext);
    }

    private List<LabelProvider> getProviders(RuleLoaderContext ruleLoaderContext)
    {
        LOG.info("Starting provider load...");
        List<LabelProvider> providers = new ArrayList<>();
        for (LabelProviderLoader loader : loaders)
        {
            if (ruleLoaderContext.isFileBasedRulesOnly() && !loader.isFileBased())
                continue;

            providers.addAll(loader.getProviders(ruleLoaderContext));
        }
        LOG.info("Loaded, now sorting, etc");

        ServiceLogger.logLoadedServices(LOG, LabelProvider.class, providers);

        LOG.info("Finished provider load");
        return Collections.unmodifiableList(providers);
    }

    private LabelProviderRegistry build(RuleLoaderContext ruleLoaderContext)
    {
        List<LabelProvider> providers = getProviders(ruleLoaderContext);
        LabelProviderRegistry registry = new LabelProviderRegistry();
        registry.setProviders(providers);

        for (LabelProvider provider : providers)
        {
            registry.setLabels(provider, provider.getData().getLabels());
        }
        return registry;
    }

}
