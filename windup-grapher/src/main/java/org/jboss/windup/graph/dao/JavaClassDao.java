package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.facet.JavaClassFacet;

public interface JavaClassDao extends BaseDao<JavaClassFacet> {
	public abstract JavaClassFacet getJavaClass(String qualifiedName);
}