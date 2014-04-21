package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.SpringConfigurationDao;
import org.jboss.windup.graph.model.meta.xml.SpringConfigurationFacet;

@Singleton
public class SpringConfigurationDaoImpl extends BaseDaoImpl<SpringConfigurationFacet> implements SpringConfigurationDao {

	public SpringConfigurationDaoImpl() {
		super(SpringConfigurationFacet.class);
	}
}
