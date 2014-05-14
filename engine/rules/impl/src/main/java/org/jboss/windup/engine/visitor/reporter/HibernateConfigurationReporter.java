package org.jboss.windup.engine.visitor.reporter;

import javax.inject.Inject;

import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.graph.dao.HibernateConfigurationDao;
import org.jboss.windup.graph.model.meta.xml.HibernateConfigurationFacetModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays all doctypes found when running Windup.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class HibernateConfigurationReporter extends AbstractGraphVisitor
{

    private static final Logger LOG = LoggerFactory.getLogger(HibernateConfigurationReporter.class);

    @Inject
    private HibernateConfigurationDao hibernateConfigurationDao;
    
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORTING;
    }

    @Override
    public void run()
    {
        for (HibernateConfigurationFacetModel hibernate : hibernateConfigurationDao.getAll())
        {
            LOG.info("Hibernate Config: ");
            LOG.info("  - Hiberate " + hibernate.getSpecificationVersion());
        }
    }
}
