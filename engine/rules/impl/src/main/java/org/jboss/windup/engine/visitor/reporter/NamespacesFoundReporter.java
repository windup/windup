package org.jboss.windup.engine.visitor.reporter;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.NamespaceDao;
import org.jboss.windup.graph.model.meta.xml.NamespaceMetaModel;
import org.jboss.windup.graph.model.resource.ArchiveEntryResourceModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows the XML Namespaces used in the application and references to the XML files which contain the namespace.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class NamespacesFoundReporter extends AbstractGraphVisitor
{

    private static final Logger LOG = LoggerFactory.getLogger(NamespacesFoundReporter.class);

    @Inject
    private NamespaceDao namespaceDao;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.REPORTING;
    }

    @Override
    public void run()
    {
        for (NamespaceMetaModel namespace : namespaceDao.getAll())
        {
            LOG.info("Namespace: ");
            LOG.info("  - URI: " + namespace.getURI());
            LOG.info("  - Loc: " + namespace.getSchemaLocation());

            for (XmlResourceModel xml : namespace.getXmlResources())
            {
                // report the xml files that contain the namespace...
                ResourceModel resource = xml.getResource();
                if (resource instanceof ArchiveEntryResourceModel)
                {
                    ArchiveEntryResourceModel ar = (ArchiveEntryResourceModel) resource;
                    LOG.info("   - " + ar.getArchiveEntry());
                }
            }
        }
    }
}
