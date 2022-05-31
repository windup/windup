package org.jboss.windup.graph;

import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.Traversable;
import com.syncleus.ferma.WrappedFramedGraph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.janusgraph.core.JanusGraph;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.Service;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.Map;

/**
 * Context for interacting with the underlying graph database API.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka, I</a>
 */
public interface GraphContext extends Closeable {
    /**
     * Get the {@link Path} on disk where this graph is stored.
     */
    Path getGraphDirectory();

    /**
     * Get the underlying {@link TinkerGraph}, which is itself a wrapper for a {@link JanusGraph}.
     */
    JanusGraph getGraph();

    /**
     * Creates new graph using the configuration. In case there was already a graph located in the specified path, it will be deleted.
     * <p>
     * If the enableListeners flag is false, then this will not enable GraphMutation listeners. These are not
     * necessary for processes that only read from the graph and are only used for graphs that are used as part of an analysis.
     */
    GraphContext create(boolean enableListeners);

    /**
     * Loads the graph using the configuration.
     */
    GraphContext load();

    /**
     * Get the {@link FramedGraph} view of the underlying {@link TinkerGraph}.
     */
    WrappedFramedGraph<JanusGraph> getFramed();

    /**
     * Get the {@link GraphTypeManager}.
     */
    GraphTypeManager getGraphTypeManager();

    /**
     * Get the {@link GraphModelScanner}.
     */
    Traversable<?, ?> getQuery(Class<? extends WindupVertexFrame> kind);

    /**
     * Clear all data from the graph (note: the graph must be closed for this operation to succeed)
     */
    void clear();

    /**
     * Sets the global configuration options to the provided {@link Map}.
     */
    void setOptions(Map<String, Object> options);

    /**
     * Returns the globally configured options as an immutable {@link Map}.
     * <p>
     * Example usage:
     * <pre>
     * Boolean overwrite = (Boolean) windupConfiguration.getOptionMap().get(OverwriteOption.NAME);
     * </pre>
     */
    Map<String, Object> getOptionMap();

    /**
     * Create a GraphService of given class.
     */
    <T extends WindupVertexFrame> Service<T> service(Class<T> clazz);

    /**
     * Convenience delegation to new GraphService(this)
     */
    <T extends WindupVertexFrame> T getUnique(Class<T> clazz);

    /**
     * Convenience delegation to new GraphService(this)
     */
    <T extends WindupVertexFrame> Iterable<T> findAll(Class<T> clazz);

    /**
     * Convenience delegation to new GraphService(this)
     */
    <T extends WindupVertexFrame> T create(Class<T> clazz);

    /**
     * Commit the current transaction.
     */
    void commit();

    /**
     * Registers a graph listener to receive events upon graph changes.
     */
    void registerGraphListener(GraphListener listener);
}
