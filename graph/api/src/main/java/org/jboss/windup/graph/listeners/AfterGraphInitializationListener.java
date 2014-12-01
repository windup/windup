package org.jboss.windup.graph.listeners;

import java.util.Map;

import org.jboss.windup.graph.GraphContext;

/**
 * Listen to events related to {@link GraphContext} initialization.
 */
public interface AfterGraphInitializationListener
{

    /**
     * Called after the {@link GraphContext} has been initialized.
     */
    void afterGraphStarted(Map<String, Object> graphConfiguration, GraphContext graphContext);

}
