package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.rules.apps.ejb.dao.SpringBeanDao;
import org.jboss.windup.rules.apps.ejb.model.SpringBeanFacetModel;

@Singleton
public class SpringBeanDaoImpl extends BaseDaoImpl<SpringBeanFacetModel> implements SpringBeanDao {

	public SpringBeanDaoImpl() {
		super(SpringBeanFacetModel.class);
	}
}
