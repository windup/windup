package org.jboss.windup.rules.apps.ejb.dao;

import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.graph.model.JNDIReferenceModel;

public interface JNDIReferenceDao extends BaseDao<JNDIReferenceModel> {
	public JNDIReferenceModel createJndiReference(String jndiLocation);
}
