package org.jboss.windup.graph.dao;

import javax.inject.Singleton;

import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacet;

@Singleton
public class EJBConfigurationDaoBean extends BaseDaoBean<EjbConfigurationFacet> {
	public EJBConfigurationDaoBean() {
		super(EjbConfigurationFacet.class);
	}
}
