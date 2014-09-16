package org.jboss.windup.config.loader;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.common.services.ServiceLoader;
import org.ocpsoft.common.util.Iterators;

public class DefaultWindupRuleProviderLoader implements WindupRuleProviderLoader
{
    @Override
    @SuppressWarnings("unchecked")
    public List<WindupRuleProvider> getProviders(GraphContext context)
    {
        return Iterators.asList(ServiceLoader.load(WindupRuleProvider.class));
    }

}
