package org.jboss.windup.graph.model;

/**
 * GraphService.createInMemory() returns objects that implement this interface. The attach method permanently stores the
 * object in the graph.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public interface InMemoryVertexFrame
{
    void attachToGraph();
}
