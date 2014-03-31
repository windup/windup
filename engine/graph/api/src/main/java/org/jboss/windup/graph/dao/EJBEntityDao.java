package org.jboss.windup.graph.dao;

import javax.inject.Singleton;

import org.jboss.windup.graph.model.meta.javaclass.EjbEntityFacet;

@Singleton
public class EJBEntityDao extends BaseDao<EjbEntityFacet> {
	public EJBEntityDao() {
		super(EjbEntityFacet.class);
	}
}
