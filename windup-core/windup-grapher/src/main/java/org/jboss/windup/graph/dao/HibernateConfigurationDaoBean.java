package org.jboss.windup.graph.dao;

import javax.inject.Singleton;

import org.jboss.windup.graph.model.meta.xml.HibernateConfigurationFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HibernateConfigurationDaoBean extends BaseDaoBean<HibernateConfigurationFacet> {
	private static final Logger LOG = LoggerFactory.getLogger(HibernateConfigurationDaoBean.class);
	public HibernateConfigurationDaoBean() {
		super(HibernateConfigurationFacet.class);
	}
}
