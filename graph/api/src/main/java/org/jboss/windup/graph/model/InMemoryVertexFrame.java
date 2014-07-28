package org.jboss.windup.graph.model;

import com.tinkerpop.frames.FramedGraph;

/**
 * GraphService.createInMemory() returns objects that implement this interface. The attach method permanently stores the
 * object in the graph.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public interface InMemoryVertexFrame
{
    void attach(FramedGraph<?> framed);
}
