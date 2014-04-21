package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.JNDIReference;

public interface JNDIReferenceDao extends BaseDao<JNDIReference> {
	public JNDIReference createJndiReference(String jndiLocation);
}
