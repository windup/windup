package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.JNDIReferenceDao;
import org.jboss.windup.graph.model.meta.JNDIReferenceModel;

@Singleton
public class JNDIReferenceDaoImpl extends BaseDaoImpl<JNDIReferenceModel> implements JNDIReferenceDao {

	public JNDIReferenceDaoImpl() {
		super(JNDIReferenceModel.class);
	}
	

	public JNDIReferenceModel createJndiReference(String jndiLocation) {
		JNDIReferenceModel ref = getByUniqueProperty("jndiLocation", jndiLocation);
		
		if(ref == null) {
			ref = create();
			ref.setJndiLocation(jndiLocation);
		}
		
		return ref;
	}
}
