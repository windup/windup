package org.jboss.windup.addon.config.impl.graphsearch;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.windup.addon.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.addon.config.graphsearch.GraphSearchFactory;

@ApplicationScoped
public class GraphSearchFactoryImpl implements GraphSearchFactory
{
    @Override
    public GraphSearchConditionBuilder create(String variableName)
    {
        return GraphSearchConditionBuilderImpl.create(variableName);
    }
}
