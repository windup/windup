package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.HibernateEntityDao;
import org.jboss.windup.graph.model.meta.javaclass.HibernateEntityFacetModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HibernateEntityDaoImpl extends BaseDaoImpl<HibernateEntityFacetModel> implements HibernateEntityDao {
	private static final Logger LOG = LoggerFactory.getLogger(HibernateEntityDaoImpl.class);
	
	public HibernateEntityDaoImpl() {
		super(HibernateEntityFacetModel.class);
	}
}
