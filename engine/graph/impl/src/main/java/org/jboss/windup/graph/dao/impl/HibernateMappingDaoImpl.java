package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.HibernateMappingDao;
import org.jboss.windup.graph.model.meta.xml.HibernateMappingFacet;

@Singleton
public class HibernateMappingDaoImpl extends BaseDaoImpl<HibernateMappingFacet> implements HibernateMappingDao {
	public HibernateMappingDaoImpl() {
		super(HibernateMappingFacet.class);
	}
}
