package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.rules.apps.ejb.dao.SpringConfigurationDao;
import org.jboss.windup.rules.apps.ejb.model.meta.xml.SpringConfigurationFacetModel;

@Singleton
public class SpringConfigurationDaoImpl extends BaseDaoImpl<SpringConfigurationFacetModel> implements SpringConfigurationDao {

	public SpringConfigurationDaoImpl() {
		super(SpringConfigurationFacetModel.class);
	}
}
