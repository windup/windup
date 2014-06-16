package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.rules.apps.ejb.dao.EJBSessionBeanDao;
import org.jboss.windup.rules.apps.ejb.model.EjbSessionBeanFacetModel;

@Singleton
public class EJBSessionBeanDaoImpl extends BaseDaoImpl<EjbSessionBeanFacetModel> implements EJBSessionBeanDao {
	public EJBSessionBeanDaoImpl() {
		super(EjbSessionBeanFacetModel.class);
	}
}
