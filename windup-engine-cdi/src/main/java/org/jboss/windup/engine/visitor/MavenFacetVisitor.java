package org.jboss.windup.engine.visitor;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.XmlResourceDaoBean;
import org.jboss.windup.graph.model.resource.XmlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Adds the MavenFacet to the XML.
 * 
 * @author bradsdavis
 *
 */
public class MavenFacetVisitor extends EmptyGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(MavenFacetVisitor.class);
	
	@Inject
	private XmlResourceDaoBean xmlResourceDao;
	
	@Override
	public void visit() {
		//visit all XML files that have a maven namespace...
		for(XmlResource entry : xmlResourceDao.containsNamespaceURI("http://maven.apache.org/POM/4.0.0")) {
			visitXmlResource(entry);
		}
	}
	
	@Override
	public void visitXmlResource(XmlResource entry) {
		LOG.info("Resource: "+entry.getResource().asVertex());
		try {
			Document document = xmlResourceDao.asDocument(entry);
			LOG.info("Successfully read XML..");
		} catch (Exception e) {
			LOG.error("Exception reading document.", e);
		}
	}
}
