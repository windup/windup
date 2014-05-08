package org.jboss.windup.addon.config.graphsearch;

public interface GraphSearchFactory
{
    public GraphSearchConditionBuilder create(String variableName);
}
