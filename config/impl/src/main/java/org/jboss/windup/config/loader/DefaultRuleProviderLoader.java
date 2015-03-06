package org.jboss.windup.config.loader;

import java.util.List;

import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.common.services.ServiceLoader;
import org.ocpsoft.common.util.Iterators;

public class DefaultRuleProviderLoader implements RuleProviderLoader
{
    @Override
    @SuppressWarnings("unchecked")
    public List<RuleProvider> getProviders(GraphContext context)
    {
        return Iterators.asList(ServiceLoader.load(RuleProvider.class));
    }

}
