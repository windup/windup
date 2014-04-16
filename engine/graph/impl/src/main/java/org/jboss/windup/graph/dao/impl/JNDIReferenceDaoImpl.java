package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.JNDIReferenceDao;
import org.jboss.windup.graph.model.meta.JNDIReference;

@Singleton
public class JNDIReferenceDaoImpl extends BaseDaoImpl<JNDIReference> implements JNDIReferenceDao {

	public JNDIReferenceDaoImpl() {
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
