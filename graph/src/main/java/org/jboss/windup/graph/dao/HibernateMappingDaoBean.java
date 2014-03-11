package org.jboss.windup.graph.dao;

import javax.inject.Singleton;

import org.jboss.windup.graph.model.meta.xml.HibernateMappingFacet;

@Singleton
public class HibernateMappingDaoBean extends BaseDaoBean<HibernateMappingFacet> {
	public HibernateMappingDaoBean() {
		super(HibernateMappingFacet.class);
	}
}
