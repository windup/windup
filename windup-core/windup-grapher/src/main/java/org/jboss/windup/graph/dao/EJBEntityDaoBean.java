package org.jboss.windup.graph.dao;

import javax.inject.Singleton;

import org.jboss.windup.graph.model.meta.javaclass.EjbEntityFacet;

@Singleton
public class EJBEntityDaoBean extends BaseDaoBean<EjbEntityFacet> {
	public EJBEntityDaoBean() {
		super(EjbEntityFacet.class);
	}
}
