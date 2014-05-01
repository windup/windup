package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.JNDIReferenceModel;

public interface JNDIReferenceDao extends BaseDao<JNDIReferenceModel> {
	public JNDIReferenceModel createJndiReference(String jndiLocation);
}
