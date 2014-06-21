package org.jboss.windup.rules.apps.ejb.dao.impl;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.dao.impl.BaseDaoImpl;
import org.jboss.windup.rules.apps.ejb.dao.EnvironmentReferenceDao;
import org.jboss.windup.graph.model.EnvironmentReferenceModel;

@Singleton
public class EnvironmentReferenceDaoImpl extends BaseDaoImpl<EnvironmentReferenceModel> implements EnvironmentReferenceDao {

	public EnvironmentReferenceDaoImpl() {
		super(EnvironmentReferenceModel.class);
	}
	

	public EnvironmentReferenceModel createEnvironmentReference(String name, String type) {
		EnvironmentReferenceModel meta = findByNameAndType(name, type);
		if(meta == null) {
			meta  = create();
			
			name = StringUtils.trim(name);
			type = StringUtils.trim(type);
			
			meta.setName(name);
			meta.setReferenceType(type);
		}
		
		return meta;
	}
	
	public EnvironmentReferenceModel findByNameAndType(String name, String type) {
		//return the first.
		for(EnvironmentReferenceModel env : hasAllProperties(
				new String[]{"name", "referenceType"}, 
				new String[]{name, type})) {
			
			return env;
		}
		return null;
	}
	
}
