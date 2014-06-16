package org.jboss.windup.rules.apps.ejb.dao.impl;

import javax.inject.Singleton;
import org.jboss.windup.graph.dao.impl.BaseDaoImpl;

import org.jboss.windup.rules.apps.ejb.dao.HibernateConfigurationDao;
import org.jboss.windup.rules.apps.ejb.model.meta.xml.HibernateConfigurationFacetModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HibernateConfigurationDaoImpl extends BaseDaoImpl<HibernateConfigurationFacetModel> implements HibernateConfigurationDao {
    
	private static final Logger LOG = LoggerFactory.getLogger(HibernateConfigurationDaoImpl.class);
	
	public HibernateConfigurationDaoImpl() {
		super(HibernateConfigurationFacetModel.class);
	}
}
