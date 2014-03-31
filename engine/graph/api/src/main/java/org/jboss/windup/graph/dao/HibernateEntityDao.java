package org.jboss.windup.graph.dao;

import javax.inject.Singleton;

import org.jboss.windup.graph.model.meta.javaclass.HibernateEntityFacet;
import org.jboss.windup.graph.model.meta.xml.HibernateConfigurationFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HibernateEntityDao extends BaseDao<HibernateEntityFacet> {
	private static final Logger LOG = LoggerFactory.getLogger(HibernateEntityDao.class);
	public HibernateEntityDao() {
		super(HibernateEntityFacet.class);
	}
}
