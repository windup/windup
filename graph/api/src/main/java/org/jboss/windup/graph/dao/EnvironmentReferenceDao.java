package org.jboss.windup.graph.dao;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.model.meta.EnvironmentReference;

public class EnvironmentReferenceDao extends BaseDao<EnvironmentReference> {

	public EnvironmentReferenceDao() {
		super(EnvironmentReference.class);
	}
	

	public EnvironmentReference createEnvironmentReference(String name, String type) {
		EnvironmentReference meta = findByNameAndType(name, type);
		if(meta == null) {
			meta  = create(null);
			
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
