package org.jboss.windup.graph;

import java.io.File;

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
     * Get the underlying {@link EventGraph}, which is itself a wrapper for a {@link TitanGraph}.
     */
    public EventGraph<TitanGraph> getGraph();

    /**
     * Get the {@link FramedGraph} view of the underlying {@link EventGraph}.
     */
    public FramedGraph<EventGraph<TitanGraph>> getFramed();

    /**
     * Get the {@link GraphTypeRegistry}.
     */
    public GraphTypeRegistry getGraphTypeRegistry();

    /**
     * Get the location on disk where the underlying {@link TitanGraph} is stored.
     */
    public File getDiskCacheDirectory();

    /**
     * Returns a {@link Service} object that is specialized for the provided type.
     */
    public <T extends VertexFrame, S extends Service<T>> S getService(Class<T> type);
}
