package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.HibernateConfigurationDao;
import org.jboss.windup.graph.model.meta.xml.HibernateConfigurationFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HibernateConfigurationDaoImpl extends BaseDaoImpl<HibernateConfigurationFacet> implements HibernateConfigurationDao {
    
	private static final Logger LOG = LoggerFactory.getLogger(HibernateConfigurationDaoImpl.class);
	
	public HibernateConfigurationDaoImpl() {
		super(HibernateConfigurationFacet.class);
	}
}
