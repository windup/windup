package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.EnvironmentReferenceModel;

public interface EnvironmentReferenceDao extends BaseDao<EnvironmentReferenceModel> {

	public EnvironmentReferenceModel createEnvironmentReference(String name, String type);
	
	public EnvironmentReferenceModel findByNameAndType(String name, String type);
	
}
