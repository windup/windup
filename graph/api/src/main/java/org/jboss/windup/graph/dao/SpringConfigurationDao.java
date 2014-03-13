package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.xml.SpringConfigurationFacet;

public class SpringConfigurationDao extends BaseDao<SpringConfigurationFacet> {

	public SpringConfigurationDao() {
		super(SpringConfigurationFacet.class);
	}
}
