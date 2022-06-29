package org.jboss.windup.graph;

import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * This is used to get a callback whenever changes occur to the graph.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface GraphListener {

    /**
     * Called when a {@link Vertex} is added to the graph.
     *
     * @param vertex
     */
    void vertexAdded(Vertex vertex);

    /**
     * This should be called whenever a vertex property is updated.
     */
    void vertexPropertyChanged(Vertex element, Property oldValue, Object setValue, Object... vertexPropertyKeyValues);
}
