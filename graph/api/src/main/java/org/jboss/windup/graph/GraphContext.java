package org.jboss.windup.graph;

import java.nio.file.Path;

import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;
import org.jboss.windup.graph.service.Service;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.VertexFrame;

/**
 * Context for interacting with the underlying graph database API.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface GraphContext
{
    /**
     * Species the directory in which to place the graph.
     * 
     * If one is not specified. This should be called before any attempts to get the graph (via getGraph or other
     * accessors). If the graph has already been initialized, this call will fail.
     * 
     * NOTE: All files in this directory will be deleted!
     */
    void setGraphDirectory(Path graphDirectory);

    /**
     * Get the location on disk where the underlying {@link TitanGraph} is stored.
     */
    Path getGraphDirectory();

    /**
     * Get the underlying {@link EventGraph}, which is itself a wrapper for a {@link TitanGraph}.
     */
    EventGraph<TitanGraph> getGraph();

    /**
     * Get the {@link FramedGraph} view of the underlying {@link EventGraph}.
     */
    FramedGraph<EventGraph<TitanGraph>> getFramed();

    /**
     * Get the {@link GraphTypeRegistry}.
     */
    GraphTypeRegistry getGraphTypeRegistry();

    /**
     * Get the {@link GraphTypeRegistry}.
     */
    TypeAwareFramedGraphQuery getQuery();

    /**
     * Returns a {@link Service} object that is specialized for the provided type.
     */
    <T extends VertexFrame, S extends Service<T>> S getService(Class<T> type);
}
