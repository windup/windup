package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.dao.EnvironmentReferenceDao;
import org.jboss.windup.graph.model.meta.EnvironmentReference;

@Singleton
public class EnvironmentReferenceDaoImpl extends BaseDaoImpl<EnvironmentReference> implements EnvironmentReferenceDao {

	public EnvironmentReferenceDaoImpl() {
		super(EnvironmentReference.class);
	}
	

	public EnvironmentReference createEnvironmentReference(String name, String type) {
		EnvironmentReference meta = findByNameAndType(name, type);
		if(meta == null) {
			meta  = create();
			
			name = StringUtils.trim(name);
			type = StringUtils.trim(type);
			
			meta.setName(name);
			meta.setReferenceType(type);
		}
		
		return meta;
	}
	
	public EnvironmentReference findByNameAndType(String name, String type) {
		//return the first.
		for(EnvironmentReference env : hasAllProperties(
				new String[]{"name", "referenceType"}, 
				new String[]{name, type})) {
			
			return env;
		}
		return null;
	}
	
}
