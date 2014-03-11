package org.jboss.windup.engine.visitor.reporter;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.HibernateConfigurationDaoBean;
import org.jboss.windup.graph.dao.HibernateEntityDaoBean;
import org.jboss.windup.graph.model.meta.javaclass.HibernateEntityFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays all Hibernate Entity classes.
 *  
 * @author bradsdavis@gmail.com
 *
 */
public class HibernateEntityReporter extends EmptyGraphVisitor {

	private static final Logger LOG = LoggerFactory.getLogger(HibernateEntityReporter.class);
	
	@Inject
	private HibernateEntityDaoBean hibernateEntityDao;
	
	@Override
	public void run() {
		for(HibernateEntityFacet hibernate : hibernateEntityDao.getAll()) {
			LOG.info("Hibernate Entity: "+hibernate.getJavaClassFacet().getQualifiedName());
			LOG.info("  - Specification: "+hibernate.getSpecificationVersion());
			LOG.info("  - Table: "+hibernate.getTableName());
			LOG.info("  - Schema: "+hibernate.getSchemaName());
			LOG.info("  - Catalog: "+hibernate.getCatalogName());
		}
	}
}
