package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.EJBEntityDao;
import org.jboss.windup.graph.model.meta.javaclass.EjbEntityFacet;

@Singleton
public class EJBEntityDaoImpl extends BaseDaoImpl<EjbEntityFacet> implements EJBEntityDao {
	public EJBEntityDaoImpl() {
		super(EjbEntityFacet.class);
	}
}
