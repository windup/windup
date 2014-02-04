package org.jboss.windup.graph.dao;

import java.util.Iterator;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.exception.NonUniqueResultException;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;


public class BaseDaoBean<T extends VertexFrame> {

	protected final Class<T> type;
	protected final String typeValue;
	protected final GraphContext context;
	
	public BaseDaoBean(GraphContext context, Class<T> type) {
		this.context = context;
		this.type = type;
		
		TypeValue typeValue = type.getAnnotation(TypeValue.class);
		if(typeValue == null) {
			throw new IllegalArgumentException("Must contain annotation 'TypeValue'");
		}
		this.typeValue = typeValue.value();
	}

	public void delete(T obj) {
		context.getFramed().removeVertex(obj.asVertex());
	}

	public Iterable<T> getByProperty(String key, Object value) {
		return (Iterable<T>)context.getFramed().getVertices(key, value, type);
	}

	public T create(Object id) {
		return (T)context.getFramed().addVertex(id, type);
	}

	public Iterable<T> getAll() {
		return (Iterable<T>)context.getFramed().query().has("type", typeValue).vertices(type);
	}

	public T getById(Object id) {
		return context.getFramed().getVertex(id, type);
	}

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

	public void commit() {
		this.context.getGraph().commit();
	}
}