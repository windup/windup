package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.javaclass.SpringBeanFacet;

public class SpringBeanDao extends BaseDao<SpringBeanFacet> {

	public SpringBeanDao() {
		super(SpringBeanFacet.class);
	}
}
