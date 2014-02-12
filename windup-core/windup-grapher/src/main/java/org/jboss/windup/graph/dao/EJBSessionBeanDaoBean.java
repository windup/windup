package org.jboss.windup.graph.dao;

import javax.inject.Singleton;

import org.jboss.windup.graph.model.meta.javaclass.EjbSessionBeanFacet;

@Singleton
public class EJBSessionBeanDaoBean extends BaseDaoBean<EjbSessionBeanFacet> {
	public EJBSessionBeanDaoBean() {
		super(EjbSessionBeanFacet.class);
	}
}
