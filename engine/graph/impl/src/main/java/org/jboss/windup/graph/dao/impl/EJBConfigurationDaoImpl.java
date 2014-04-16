package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.EJBConfigurationDao;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacet;

@Singleton
public class EJBConfigurationDaoImpl extends BaseDaoImpl<EjbConfigurationFacet> implements EJBConfigurationDao {
	public EJBConfigurationDaoImpl() {
		super(EjbConfigurationFacet.class);
	}
}
