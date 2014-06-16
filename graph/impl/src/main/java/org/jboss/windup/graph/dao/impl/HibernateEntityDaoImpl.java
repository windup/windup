package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.rules.apps.ejb.dao.HibernateEntityDao;
import org.jboss.windup.rules.apps.ejb.model.HibernateEntityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HibernateEntityDaoImpl extends BaseDaoImpl<HibernateEntityModel> implements HibernateEntityDao {
	private static final Logger LOG = LoggerFactory.getLogger(HibernateEntityDaoImpl.class);
	
	public HibernateEntityDaoImpl() {
		super(HibernateEntityModel.class);
	}
}
