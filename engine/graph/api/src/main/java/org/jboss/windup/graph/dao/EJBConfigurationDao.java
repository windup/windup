package org.jboss.windup.graph.dao;

import javax.inject.Singleton;

import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacet;

@Singleton
public class EJBConfigurationDao extends BaseDao<EjbConfigurationFacet> {
	public EJBConfigurationDao() {
		super(EjbConfigurationFacet.class);
	}
}
