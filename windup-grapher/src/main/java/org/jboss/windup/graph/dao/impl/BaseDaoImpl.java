package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.graph.dao.exception.NonUniqueResultException;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;


public class BaseDaoImpl<T extends VertexFrame> implements BaseDao<T> {

	protected final Class<T> type;
	protected final String typeValue;
	protected final GraphContext context;
	
	public BaseDaoImpl(GraphContext context, Class<T> type) {
		this.context = context;
		this.type = type;
		
		TypeValue typeValue = type.getAnnotation(TypeValue.class);
		if(typeValue == null) {
			throw new IllegalArgumentException("Must contain annotation 'TypeValue'");
		}
		this.typeValue = typeValue.value();
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
		return (Iterable<T>)context.getFramed().query().has("type", typeValue).vertices(type);
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

	@Override
	public T castToType(VertexFrame v) {
		Vertex vertex = v.asVertex();
		TypeValue value = type.getAnnotation(TypeValue.class);
		TypeField field = type.getAnnotation(TypeField.class);
		
		String property = "type";
		if(field != null) {
			property = field.value();
		}
		String typeValue = this.type.getName();
		if(value != null) {
			typeValue = value.value();
		}
		
		vertex.setProperty(property, typeValue);
		context.getGraph().commit();
		return context.getFramed().frame(vertex, type);
		
	}

	@Override
	public void commit() {
		this.context.getGraph().commit();
	}
}