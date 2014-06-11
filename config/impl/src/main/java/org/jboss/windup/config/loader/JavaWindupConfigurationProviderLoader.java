package org.jboss.windup.config.loader;

import java.util.List;

import org.jboss.windup.config.WindupConfigurationProvider;
import org.ocpsoft.common.services.ServiceLoader;
import org.ocpsoft.common.util.Iterators;

public class JavaWindupConfigurationProviderLoader implements WindupConfigurationProviderLoader
{
    @Override
    @SuppressWarnings("unchecked")
    public List<WindupConfigurationProvider> getProviders()
    {
        return Iterators.asList(ServiceLoader.load(WindupConfigurationProvider.class));
    }

}
