package org.jboss.windup.graph.service;

import org.jboss.windup.graph.service.exception.NonUniqueResultException;

import com.thinkaurelius.titan.core.TitanTransaction;
import com.tinkerpop.frames.VertexFrame;

public interface Service<T extends VertexFrame>
{
    void commit();

    long count(Iterable<?> obj);

    /**
     * Create a new VertexFrame of the Service's type, but don't attach it to the graph.
     * 
     * Note that only @Property annotated methods are supported by the returned object.
     */
    T createInMemory();

    T create();

    T create(Object id);

    Iterable<T> findAll();

    Iterable<T> findAllByProperties(String[] keys, String[] vals);

    Iterable<T> findAllByProperty(String key, Object value);

    Iterable<T> findAllByPropertyMatchingRegex(String key, String... regex);

    T getById(Object id);

    T getUnique() throws NonUniqueResultException;

    T getUniqueByProperty(String property, Object value) throws NonUniqueResultException;

    TitanTransaction newTransaction();

    Class<T> getType();
}