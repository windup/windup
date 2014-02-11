package org.jboss.windup.engine.visitor.reporter;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.DoctypeDaoBean;
import org.jboss.windup.graph.dao.HibernateConfigurationDaoBean;
import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;
import org.jboss.windup.graph.model.meta.xml.HibernateConfigurationFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays all doctypes found when running Windup.
 *  
 * @author bradsdavis@gmail.com
 *
 */
public class HibernateConfigurationReporter extends EmptyGraphVisitor {

	private static final Logger LOG = LoggerFactory.getLogger(HibernateConfigurationReporter.class);
	
	@Inject
	private HibernateConfigurationDaoBean hibernateConfigurationDao;
	
	@Override
	public void run() {
		for(HibernateConfigurationFacet hibernate : hibernateConfigurationDao.getAll()) {
			LOG.info("Hibernate Config: ");
			LOG.info("  - Hiberate "+hibernate.getSpecificationVersion());
		}
	}
}
