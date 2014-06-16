package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.rules.apps.ejb.dao.HibernateMappingDao;
import org.jboss.windup.rules.apps.ejb.model.meta.xml.HibernateMappingFacetModel;

@Singleton
public class HibernateMappingDaoImpl extends BaseDaoImpl<HibernateMappingFacetModel> implements HibernateMappingDao {
	public HibernateMappingDaoImpl() {
		super(HibernateMappingFacetModel.class);
	}
}
