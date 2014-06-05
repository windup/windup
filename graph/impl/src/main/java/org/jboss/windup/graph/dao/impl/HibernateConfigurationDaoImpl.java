package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.HibernateConfigurationDao;
import org.jboss.windup.graph.model.meta.xml.HibernateConfigurationFacetModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HibernateConfigurationDaoImpl extends BaseDaoImpl<HibernateConfigurationFacetModel> implements HibernateConfigurationDao {
    
	private static final Logger LOG = LoggerFactory.getLogger(HibernateConfigurationDaoImpl.class);
	
	public HibernateConfigurationDaoImpl() {
		super(HibernateConfigurationFacetModel.class);
	}
}
