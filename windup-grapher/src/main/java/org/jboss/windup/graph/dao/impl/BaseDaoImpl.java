package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.graph.dao.exception.NonUniqueResultException;

import com.tinkerpop.frames.VertexFrame;


public class BaseDaoImpl<T extends VertexFrame> implements BaseDao<T> {

	protected final Class<T> type;
	protected final GraphContext context;
	
	public BaseDaoImpl(GraphContext context, Class<T> type) {
		this.context = context;
		this.type = type;
	}

	@Override
	public void delete(T obj) {
		context.getFramed().removeVertex(obj.asVertex());
	}

	@Override
	public Iterable<T> getByProperty(String key, Object value) {
		return (Iterable<T>)context.getFramed().getVertices(key, value, type);
	}

	@Override
	public T create(Object id) {
		return (T)context.getFramed().addVertex(id, type);
	}

	@Override
	public Iterable<T> getAll() {
		return (Iterable<T>)context.getFramed().getVertices(null, null, type);
	}

	@Override
	public T getById(Object id) {
		return context.getFramed().getVertex(id, type);
	}

	@Override
	public T getByUniqueProperty(String property, Object value) throws NonUniqueResultException {
		Iterable<T> results = getByProperty(property, value);
		
		if(!results.iterator().hasNext()) {
			return null;
		}
		
		Iterator<T> iter = results.iterator();
		T result = iter.next();
		
		if(iter.hasNext()) {
			throw new NonUniqueResultException("Expected unique value, but returned non-unique.");
		}
		
		return result;
	}
}