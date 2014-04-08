package org.jboss.windup.engine.visitor.reporter;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.EJBConfigurationDao;
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
public class EjbConfigurationReporter extends AbstractGraphVisitor
{

    private static final Logger LOG = LoggerFactory.getLogger(EjbConfigurationReporter.class);

    @Inject
    private EJBConfigurationDao ejbConfigurationDao;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.Reporting;
    }

    @Override
    public void run()
    {
        for (EjbConfigurationFacet ejb : ejbConfigurationDao.getAll())
        {
            LOG.info("Ejb Configuration: " + ejb.getXmlFacet());
            LOG.info("  - EJB Specification: " + ejb.getSpecificationVersion());

            LOG.info("  - Ejb Entities: ");
            for (EjbEntityFacet entity : ejb.getEjbEntity())
            {
                String name = "    - " + entity.getEjbEntityName();
                String clz = "";
                if (entity.getJavaClassFacet() != null)
                {
                    clz = entity.getJavaClassFacet().getQualifiedName();
                }
                LOG.info(name + " - " + clz);
            }
            LOG.info("  - Ejb Session: ");
            for (EjbSessionBeanFacet session : ejb.getEjbSessionBeans())
            {
                String name = "    - " + session.getSessionBeanName();
                String clz = "";
                if (session.getJavaClassFacet() != null)
                {
                    clz = session.getJavaClassFacet().getQualifiedName();
                }
                LOG.info(name + " - " + clz);
            }
            LOG.info("  - MDBs: ");
            for (MessageDrivenBeanFacet mdb : ejb.getMessageDriven())
            {
                String name = "    - " + mdb.getMessageDrivenBeanName();
                String clz = "";
                if (mdb.getJavaClassFacet() != null)
                {
                    clz = mdb.getJavaClassFacet().getQualifiedName();
                }
                LOG.info(name + " - " + clz);
            }
        }
    }
}
