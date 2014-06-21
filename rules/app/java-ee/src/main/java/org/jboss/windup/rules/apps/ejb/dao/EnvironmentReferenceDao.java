package org.jboss.windup.rules.apps.ejb.dao;

import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.graph.model.EnvironmentReferenceModel;

public interface EnvironmentReferenceDao extends BaseDao<EnvironmentReferenceModel> {

	public EnvironmentReferenceModel createEnvironmentReference(String name, String type);
	
	public EnvironmentReferenceModel findByNameAndType(String name, String type);
	
}
