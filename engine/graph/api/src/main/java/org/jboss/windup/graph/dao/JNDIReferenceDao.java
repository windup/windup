package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.JNDIReference;

public class JNDIReferenceDao extends BaseDao<JNDIReference> {

	public JNDIReferenceDao() {
		super(JNDIReference.class);
	}
	

	public JNDIReference createJndiReference(String jndiLocation) {
		JNDIReference ref = getByUniqueProperty("jndiLocation", jndiLocation);
		
		if(ref == null) {
			ref = create();
			ref.setJndiLocation(jndiLocation);
		}
		
		return ref;
	}
}
