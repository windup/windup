package org.jboss.windup.graph.dao;

import javax.inject.Singleton;

import org.jboss.windup.graph.model.meta.xml.HibernateMappingFacet;

@Singleton
public class HibernateMappingDao extends BaseDao<HibernateMappingFacet> {
	public HibernateMappingDao() {
		super(HibernateMappingFacet.class);
	}
}
