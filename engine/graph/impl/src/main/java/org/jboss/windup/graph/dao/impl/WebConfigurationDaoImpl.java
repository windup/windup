package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.WebConfigurationDao;
import org.jboss.windup.graph.model.meta.xml.WebConfigurationFacet;

@Singleton
public class WebConfigurationDaoImpl extends BaseDaoImpl<WebConfigurationFacet> implements WebConfigurationDao {

	public WebConfigurationDaoImpl() {
		super(WebConfigurationFacet.class);
	}
}
