package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.HibernateMappingDao;
import org.jboss.windup.graph.model.meta.xml.HibernateMappingFacetModel;

@Singleton
public class HibernateMappingDaoImpl extends BaseDaoImpl<HibernateMappingFacetModel> implements HibernateMappingDao {
	public HibernateMappingDaoImpl() {
		super(HibernateMappingFacetModel.class);
	}
}
