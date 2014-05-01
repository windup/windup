package org.jboss.windup.engine.visitor.reporter;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.HibernateEntityDao;
import org.jboss.windup.graph.model.meta.javaclass.HibernateEntityFacetModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays all Hibernate Entity classes.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class HibernateEntityReporter extends AbstractGraphVisitor
{

    private static final Logger LOG = LoggerFactory.getLogger(HibernateEntityReporter.class);

    @Inject
    private HibernateEntityDao hibernateEntityDao;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.REPORTING;
    }

    @Override
    public void run()
    {
        for (HibernateEntityFacetModel hibernate : hibernateEntityDao.getAll())
        {
            LOG.info("Hibernate Entity: " + hibernate.getJavaClassFacet().getQualifiedName());
            LOG.info("  - Specification: " + hibernate.getSpecificationVersion());
            LOG.info("  - Table: " + hibernate.getTableName());
            LOG.info("  - Schema: " + hibernate.getSchemaName());
            LOG.info("  - Catalog: " + hibernate.getCatalogName());
        }
    }
}
