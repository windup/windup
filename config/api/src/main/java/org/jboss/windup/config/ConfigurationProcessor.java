package org.jboss.windup.config;

import org.jboss.windup.graph.GraphContext;

public interface ConfigurationProcessor
{
    public void run(GraphContext context);
}
