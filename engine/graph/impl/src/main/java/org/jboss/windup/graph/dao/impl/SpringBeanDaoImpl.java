package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.SpringBeanDao;
import org.jboss.windup.graph.model.meta.javaclass.SpringBeanFacet;

@Singleton
public class SpringBeanDaoImpl extends BaseDaoImpl<SpringBeanFacet> implements SpringBeanDao {

	public SpringBeanDaoImpl() {
		super(SpringBeanFacet.class);
	}
}
