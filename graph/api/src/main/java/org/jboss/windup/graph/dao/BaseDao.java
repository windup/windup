package org.jboss.windup.graph.dao;

import com.tinkerpop.frames.VertexFrame;

public interface BaseDao<T extends VertexFrame>
{
    public T create();

    public T create(Object id);

    public void delete(T obj);

    public Iterable<T> getAll();

    public long count(Iterable<?> obj);

    public T getById(Object id);

    public void commit();

}