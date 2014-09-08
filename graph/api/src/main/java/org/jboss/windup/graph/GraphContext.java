package org.jboss.windup.graph;

import java.io.Closeable;
import java.nio.file.Path;

import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.tinkerpop.frames.FramedGraph;

/**
 * Context for interacting with the underlying graph database API.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface GraphContext extends Closeable
{
    /**
     * Get the {@link Path} on disk where this graph is stored.
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
     * Clear all data from the graph (note: the graph must be closed for this operation to succeed)
     */
    public void clear();
}
