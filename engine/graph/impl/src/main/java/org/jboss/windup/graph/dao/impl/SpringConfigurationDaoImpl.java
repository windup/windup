package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.SpringConfigurationDao;
import org.jboss.windup.graph.model.meta.xml.SpringConfigurationFacetModel;

@Singleton
public class SpringConfigurationDaoImpl extends BaseDaoImpl<SpringConfigurationFacetModel> implements SpringConfigurationDao {

	public SpringConfigurationDaoImpl() {
		super(SpringConfigurationFacetModel.class);
	}
}
