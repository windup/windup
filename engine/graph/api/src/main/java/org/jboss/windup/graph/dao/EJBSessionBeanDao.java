package org.jboss.windup.graph.dao;

import javax.inject.Singleton;

import org.jboss.windup.graph.model.meta.javaclass.EjbSessionBeanFacet;

@Singleton
public class EJBSessionBeanDao extends BaseDao<EjbSessionBeanFacet> {
	public EJBSessionBeanDao() {
		super(EjbSessionBeanFacet.class);
	}
}
