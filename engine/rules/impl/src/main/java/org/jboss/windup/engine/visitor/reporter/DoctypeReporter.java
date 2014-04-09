package org.jboss.windup.engine.visitor.reporter;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.DoctypeDao;
import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays all doctypes found when running Windup.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class DoctypeReporter extends AbstractGraphVisitor
{

    private static final Logger LOG = LoggerFactory.getLogger(DoctypeReporter.class);

    @Inject
    private DoctypeDao doctypeDao;
    
    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.REPORTING;
    }

    @Override
    public void run()
    {
        for (DoctypeMeta doctype : doctypeDao.getAll())
        {
            LOG.info("Doctype: ");
            LOG.info("  - publicId [" + doctype.getPublicId() + "]");
            LOG.info("  - systemId [" + doctype.getSystemId() + "]");
        }
    }
}
