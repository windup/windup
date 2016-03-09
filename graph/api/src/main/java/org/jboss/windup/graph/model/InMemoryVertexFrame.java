package org.jboss.windup.graph.model;

import org.jboss.windup.graph.GraphContext;

/**
 * GraphService.createInMemory() returns objects that implement this interface. The attach method permanently stores the
 * object in the graph.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface InMemoryVertexFrame
{
    void attachToGraph(GraphContext context);
}
