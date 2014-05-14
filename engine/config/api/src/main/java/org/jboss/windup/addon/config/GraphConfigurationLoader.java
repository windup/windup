package org.jboss.windup.addon.config;

import java.util.List;

import org.ocpsoft.common.services.ServiceLoader;
import org.ocpsoft.common.util.Iterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphConfigurationLoader
{
    public static Logger LOG = LoggerFactory.getLogger(GraphConfigurationLoader.class);

    private final List<WindupConfigurationProvider> providers;

    public GraphConfigurationLoader(Object context)
    {
        providers = GraphProviderSorter.sort(Iterators.asList(ServiceLoader.load(WindupConfigurationProvider.class)));
    }
}
