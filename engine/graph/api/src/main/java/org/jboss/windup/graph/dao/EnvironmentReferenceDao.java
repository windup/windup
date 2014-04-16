package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.EnvironmentReference;

public interface EnvironmentReferenceDao extends BaseDao<EnvironmentReference> {

	public EnvironmentReference createEnvironmentReference(String name, String type);
	
	public EnvironmentReference findByNameAndType(String name, String type);
	
}
