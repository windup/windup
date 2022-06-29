package org.jboss.windup.config.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.lock.LockMode;
import org.jboss.forge.furnace.spi.ExportedInstance;
import org.jboss.forge.furnace.util.AddonFilters;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.metadata.RulesetMetadata;

/**
 * Default implementation of {@link RuleProviderLoader} that uses {@link Furnace} to load {@link RuleProvider} instances
 * and establish relationships with their corresponding {@link RulesetMetadata} if available.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class DefaultRuleProviderLoader implements RuleProviderLoader {
    @Inject
    private Furnace furnace;

    @Override
    public boolean isFileBased() {
        return false;
    }

    @Override
    public List<RuleProvider> getProviders(RuleLoaderContext ruleLoaderContext) {
        return furnace.getLockManager().performLocked(LockMode.READ, new Callable<List<RuleProvider>>() {
            @Override
            public List<RuleProvider> call() throws Exception {
                List<RuleProvider> result = new ArrayList<>();

                Set<Addon> addons = furnace.getAddonRegistry().getAddons(AddonFilters.allStarted());
                for (Addon addon : addons) {
                    RulesetMetadata rulesetMetadata = null;
                    ExportedInstance<RulesetMetadata> metadataInstance = addon.getServiceRegistry().getExportedInstance(RulesetMetadata.class);
                    if (metadataInstance != null) {
                        rulesetMetadata = metadataInstance.get();
                    }

                    Set<ExportedInstance<RuleProvider>> providerInstances = addon.getServiceRegistry().getExportedInstances(RuleProvider.class);
                    for (ExportedInstance<RuleProvider> instance : providerInstances) {
                        RuleProvider provider = instance.get();
                        if (provider.getMetadata() instanceof MetadataBuilder)
                            ((MetadataBuilder) provider.getMetadata()).setRulesetMetadata(rulesetMetadata);
                        result.add(provider);
                    }
                }

                return result;
            }
        });
    }
}
