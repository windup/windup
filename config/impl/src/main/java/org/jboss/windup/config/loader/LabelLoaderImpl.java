package org.jboss.windup.config.loader;

import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.LabelProvider;
import org.jboss.windup.config.metadata.LabelProviderRegistry;
import org.jboss.windup.util.ServiceLogger;
import org.jboss.windup.util.exception.WindupException;

import javax.inject.Inject;
import java.util.*;
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

    private void checkForDuplicateProviders(List<LabelProvider> providers)
    {
        /*
         * We are using a map so that we can easily pull out the previous value later (in the case of a duplicate)
         */
        Map<LabelProvider, LabelProvider> duplicates = new HashMap<>(providers.size());
        for (LabelProvider provider : providers)
        {
            LabelProvider previousProvider = duplicates.get(provider);
            if (previousProvider != null)
            {
                String typeMessage;
                String currentProviderOrigin = provider.getMetadata().getOrigin();
                String previousProviderOrigin = previousProvider.getMetadata().getOrigin();
                if (previousProvider.getClass().equals(provider.getClass()))
                {
                    typeMessage = " (type: " + previousProviderOrigin + " and " + currentProviderOrigin + ")";
                }
                else
                {
                    typeMessage = " (types: " + Proxies.unwrapProxyClassName(previousProvider.getClass()) + " at " + previousProviderOrigin
                            + " and " + Proxies.unwrapProxyClassName(provider.getClass()) + " at " + currentProviderOrigin + ")";
                }

                throw new WindupException("Found two providers with the same id: " + provider.getMetadata().getID() + typeMessage);
            }
            duplicates.put(provider, provider);
        }
    }

    private List<LabelProvider> getProviders(RuleLoaderContext ruleLoaderContext)
    {
        LOG.info("Starting provider load...");
        List<LabelProvider> unsortedProviders = new ArrayList<>();
        for (LabelProviderLoader loader : loaders)
        {
            if (ruleLoaderContext.isFileBasedRulesOnly() && !loader.isFileBased())
                continue;

            unsortedProviders.addAll(loader.getProviders(ruleLoaderContext));
        }
        LOG.info("Loaded, now sorting, etc");

        checkForDuplicateProviders(unsortedProviders);

        List<LabelProvider> sortedProviders = new ArrayList<>(unsortedProviders);
        sortedProviders.sort(Comparator.comparingInt(t -> t.getMetadata().getPriority()));
        ServiceLogger.logLoadedServices(LOG, LabelProvider.class, sortedProviders);

        LOG.info("Finished provider load");
        return Collections.unmodifiableList(sortedProviders);
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
