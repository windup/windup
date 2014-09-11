package org.jboss.windup.graph;

/**
 * Listener for graph lifecycle events.
 * Allows e.g. tests to prepare the data for a test run.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface GraphLifecycleListener {

    /**
     * Called after the graph is opened (created).
     * @param context  Use to access the graph via Blueprints or Frames APIs.
     */
    public void postOpen(GraphContext context);
    
    /**
     * Called before the graph is shut down (destroyed).
     * @param context  Use to access the graph via Blueprints or Frames APIs.
     */
    public void preShutdown(GraphContext context);

}
