package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.EJBSessionBeanDao;
import org.jboss.windup.graph.model.meta.javaclass.EjbSessionBeanFacet;

@Singleton
public class EJBSessionBeanDaoImpl extends BaseDaoImpl<EjbSessionBeanFacet> implements EJBSessionBeanDao {
	public EJBSessionBeanDaoImpl() {
		super(EjbSessionBeanFacet.class);
	}
}
