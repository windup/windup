package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.xml.WebConfigurationFacet;

public class WebConfigurationDao extends BaseDao<WebConfigurationFacet> {

	public WebConfigurationDao() {
		super(WebConfigurationFacet.class);
	}
}
