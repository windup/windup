package org.jboss.windup.graph.listeners;

/**
 * Listener listening to the event fired just before the graph is closed.
 * @author mbriskar
 *
 */
public interface BeforeGraphCloseListener
{
    /**
     * Called before closing
     */
    void beforeGraphClose();
}
