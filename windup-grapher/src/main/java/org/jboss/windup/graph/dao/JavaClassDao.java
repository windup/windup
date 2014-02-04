package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.JavaClass;

public interface JavaClassDao extends BaseDao<JavaClass> {
	public abstract JavaClass getJavaClass(String qualifiedName);
	public abstract Iterable<JavaClass> getAllClassNotFound();
	public abstract Iterable<JavaClass> getAllDuplicateClasses();
	
}