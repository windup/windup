package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.EJBSessionBeanDao;
import org.jboss.windup.graph.model.meta.javaclass.EjbSessionBeanFacetModel;

@Singleton
public class EJBSessionBeanDaoImpl extends BaseDaoImpl<EjbSessionBeanFacetModel> implements EJBSessionBeanDao {
	public EJBSessionBeanDaoImpl() {
		super(EjbSessionBeanFacetModel.class);
	}
}
