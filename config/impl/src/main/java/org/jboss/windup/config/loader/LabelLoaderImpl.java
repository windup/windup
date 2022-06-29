package org.jboss.windup.config.loader;

import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.LabelProvider;
import org.jboss.windup.config.metadata.Label;
import org.jboss.windup.config.metadata.LabelProviderRegistry;
import org.jboss.windup.config.metadata.LabelsetMetadata;
import org.jboss.windup.util.ServiceLogger;
import org.jboss.windup.util.exception.WindupException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Tales all {@link LabelProviderLoader}s available in the system and use them to load {@link LabelProvider}s.
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
public class LabelLoaderImpl implements LabelLoader {
    public static Logger LOG = Logger.getLogger(LabelLoaderImpl.class.getName());

    @Inject
    private Imported<LabelProviderLoader> loaders;

    public LabelLoaderImpl() {
    }

    /**
     * {@link LabelLoader#loadConfiguration(RuleLoaderContext)}
     */
    @Override
    public LabelProviderRegistry loadConfiguration(RuleLoaderContext ruleLoaderContext) {
        return build(ruleLoaderContext);
    }

    /**
     * Multiple 'Labelsets' with the same ID are not allowed. See {@link LabelsetMetadata#getID()}
     *
     * @throws WindupException in case there are multiple 'Labelsets' using the same ID
     **/
    private void checkForDuplicateProviders(List<LabelProvider> providers) {
        /*
         * LabelsetMetadata We are using a map so that we can easily pull out the previous value later (in the case of a duplicate)
         */
        Map<LabelProvider, LabelProvider> duplicates = new HashMap<>(providers.size());
        for (LabelProvider provider : providers) {
            LabelProvider previousProvider = duplicates.get(provider);
            if (previousProvider != null) {
                String typeMessage;
                String currentProviderOrigin = provider.getMetadata().getOrigin();
                String previousProviderOrigin = previousProvider.getMetadata().getOrigin();
                if (previousProvider.getClass().equals(provider.getClass())) {
                    typeMessage = " (type: " + previousProviderOrigin + " and " + currentProviderOrigin + ")";
                } else {
                    typeMessage = " (types: " + Proxies.unwrapProxyClassName(previousProvider.getClass()) + " at " + previousProviderOrigin
                            + " and " + Proxies.unwrapProxyClassName(provider.getClass()) + " at " + currentProviderOrigin + ")";
                }

                throw new WindupException("Found two providers with the same id: " + provider.getMetadata().getID() + typeMessage);
            }
            duplicates.put(provider, provider);
        }
    }

    /**
     * Returns all the {@link LabelProvider} found inside the folders described in {@link RuleLoaderContext}
     */
    private List<LabelProvider> getProviders(RuleLoaderContext ruleLoaderContext) {
        LOG.info("Starting provider load...");
        List<LabelProvider> unsortedProviders = new ArrayList<>();
        for (LabelProviderLoader loader : loaders) {
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

    /**
     * Loads all {@link LabelProvider} within {@link RuleLoaderContext}
     **/
    private LabelProviderRegistry build(RuleLoaderContext ruleLoaderContext) {
        List<LabelProvider> providers = getProviders(ruleLoaderContext);
        LabelProviderRegistry registry = new LabelProviderRegistry();
        registry.setProviders(providers);

        for (LabelProvider provider : providers) {
            if (ruleLoaderContext.getLabelProviderFilter() != null) {
                boolean accepted = ruleLoaderContext.getLabelProviderFilter().accept(provider);
                LOG.info((accepted ? "Accepted" : "Skipped") + ": [" + provider + "] by filter [" + ruleLoaderContext.getLabelProviderFilter() + "]");
                if (!accepted)
                    continue;
            }

            List<Label> labels = provider.getData().getLabels();

            // Validate repeated IDs
            List<String> labelIDs = labels.stream().map(Label::getId)
                    .collect(Collectors.toList());
            List<Label> repeatedLabels = labels.stream()
                    .filter(i -> Collections.frequency(labelIDs, i.getId()) > 1)
                    .collect(Collectors.toList());
            if (!repeatedLabels.isEmpty()) {
                throw new WindupException("Found multiple labels with the same id: " +
                        repeatedLabels.stream().map(Label::getId).distinct().collect(Collectors.joining(",")) +
                        " within the same labelSet: " + provider.getMetadata().getID());
            }

            // Validate Label Bean
            List<Label> invalidLabels = labels
                    .stream()
                    .filter(p -> Objects.isNull(p.getId()) ||
                            Objects.isNull(p.getName()) ||
                            p.getId().trim().isEmpty() ||
                            p.getName().trim().isEmpty())
                    .collect(Collectors.toList());
            if (!invalidLabels.isEmpty()) {
                throw new WindupException("Found invalid labels: " +
                        invalidLabels.stream().map(Label::getId).collect(Collectors.joining(",")) +
                        " within the labelSet:" + provider.getMetadata().getID()
                        + ". Label[id] and Label[name] name should not be null or empty");
            }

            registry.setLabels(provider, labels);
        }
        return registry;
    }

}
