package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.dao.exception.NonUniqueResultException;

import com.tinkerpop.frames.VertexFrame;


public interface BaseDao<T extends VertexFrame> {
	public void delete(final T obj);
	public Iterable<T> getByProperty(String key, Object value);
	public T getById(Object id);
	public T getByUniqueProperty(String property, Object value) throws NonUniqueResultException;
	public T create(Object id);
	public Iterable<T> getAll();
}
