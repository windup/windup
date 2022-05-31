package org.jboss.windup.graph.service;

import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.exception.NonUniqueResultException;

import java.util.List;

/**
 * Base service interface for interacting with {@link WindupVertexFrame} instances.
 *
 * @param <FRAMETYPE>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface Service<FRAMETYPE extends WindupVertexFrame> {
    /**
     * Commit any started transaction.
     *
     * @see #newTransaction()
     */
    void commit();

    /**
     * Create a new instance of the {@link WindupVertexFrame} type on which this {@link Service} operates. The returned instance will already be
     * persisted in the graph.
     */
    FRAMETYPE create();

    /**
     * Adds the type of this service to the provided {@link WindupVertexFrame}, and returns the result
     */
    FRAMETYPE addTypeToModel(WindupVertexFrame model);

    /**
     * Remove the given {@link WindupVertexFrame} instance from the graph.
     */
    void remove(FRAMETYPE model);

    /**
     * Find all instances of the {@link WindupVertexFrame} type on which this {@link Service} operates.
     */
    List<FRAMETYPE> findAll();

    /**
     * Find all instances of the {@link WindupVertexFrame} type on which this {@link Service} operates with the given properties.
     */
    Iterable<FRAMETYPE> findAllByProperties(String[] keys, String[] vals);

    /**
     * Find all instances of the {@link WindupVertexFrame} type on which this {@link Service} operates with the given property.
     */
    Iterable<FRAMETYPE> findAllByProperty(String key, Object value);

    /**
     * Find all instances of the {@link WindupVertexFrame} type on which this {@link Service} operates with the given property not set to the given
     * value.
     */
    Iterable<FRAMETYPE> findAllWithoutProperty(final String key, final Object value);

    /**
     * Find all instances of the {@link WindupVertexFrame} type on which this {@link Service} operates with the given property.
     */
    Iterable<FRAMETYPE> findAllWithoutProperty(final String key);

    /**
     * Find all instances of the {@link WindupVertexFrame} type on which this {@link Service} operates with the specified property matching the given
     * regexes.
     */
    Iterable<FRAMETYPE> findAllByPropertyMatchingRegex(String key, String... regex);

    /**
     * Get the instance of the type on which this {@link Service} operates that has the given id value.
     */
    FRAMETYPE getById(Object id);

    /**
     * Get a unique instance of the type on which this {@link Service} operates.
     */
    FRAMETYPE getUnique() throws NonUniqueResultException;

    /**
     * Search the graph for a model of the appropriate type with the given property name and value. Return <code>null</code> if not found.
     */
    FRAMETYPE getUniqueByProperty(String property, Object value) throws NonUniqueResultException;

    /**
     * Begin a transaction.
     *
     * @see #commit()
     */
    Transaction newTransaction();

    /**
     * Get the {@link WindupVertexFrame} type for which this {@link Service} operates.
     */
    Class<FRAMETYPE> getType();

    /**
     * Return the given {@link Vertex} as a {@link WindupVertexFrame} (if possible.)
     * <p>
     * <b>Note:</b> This method will always succeed! Even if the given {@link Vertex} is not actually the specified {@link WindupVertexFrame} type.
     * Call {@link GraphTypeManager#hasType(Class, Vertex)} <b>before</b> using this!
     */
    FRAMETYPE frame(Vertex vertex);
}
