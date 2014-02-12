package org.jboss.windup.engine.visitor.reporter;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.EJBConfigurationDaoBean;
import org.jboss.windup.graph.model.meta.javaclass.EjbEntityFacet;
import org.jboss.windup.graph.model.meta.javaclass.EjbSessionBeanFacet;
import org.jboss.windup.graph.model.meta.javaclass.MessageDrivenBeanFacet;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays all EJB Configurations found when running Windup.
 *  
 * @author bradsdavis@gmail.com
 *
 */
public class EjbConfigurationReporter extends EmptyGraphVisitor {

	private static final Logger LOG = LoggerFactory.getLogger(EjbConfigurationReporter.class);
	
	@Inject
	private EJBConfigurationDaoBean ejbConfigurationDao;
	
	@Override
	public void run() {
		for(EjbConfigurationFacet ejb : ejbConfigurationDao.getAll()) {
			LOG.info("Ejb Configuration: ");
			LOG.info("  - EJB Specification: "+ejb.getSpecificationVersion());
			
			
			LOG.info("  - Ejb Entities: ");
			for(EjbEntityFacet entity : ejb.getEjbEntity()) {
				LOG.info("    - ["+entity.getEjbEntityName()+"] - "+entity.getJavaClassFacet().getQualifiedName());
				
			}
			LOG.info("  - Ejb Session: ");
			for(EjbSessionBeanFacet session : ejb.getEjbSessionBeans()) {
				LOG.info("    - ["+session.getSessionBeanName()+"] - "+session.getJavaClassFacet().getQualifiedName());
			}
			LOG.info("  - MDBs: ");
			for(MessageDrivenBeanFacet mdb : ejb.getMessageDriven()) {
				LOG.info("    - ["+mdb.getMessageDrivenBeanName()+"] - "+mdb.getJavaClassFacet().getQualifiedName());
			}
		}
	}
}
