package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.JavaClass;

public interface ClassGraphDao {

	public abstract JavaClass getJavaClass(String qualifiedName);

}