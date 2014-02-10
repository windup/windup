package org.jboss.windup.engine.visitor;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jboss.windup.engine.util.xml.LocationAwareContentHandler;
import org.jboss.windup.engine.util.xml.LocationAwareContentHandler.Doctype;
import org.jboss.windup.engine.util.xml.LocationAwareXmlReader;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.ArchiveEntryDaoBean;
import org.jboss.windup.graph.dao.DoctypeDaoBean;
import org.jboss.windup.graph.dao.NamespaceDaoBean;
import org.jboss.windup.graph.dao.XmlResourceDaoBean;
import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.jboss.windup.graph.model.resource.XmlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Adds the XMLResource Facet to the resource.
 * Extracts Doctype and Namespace information in the XML files.
 * 
 * @author bradsdavis@gmail.com
 *
 */
public class XmlResourceVisitor extends EmptyGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(XmlResourceVisitor.class);
	
	@Inject
	private DoctypeDaoBean doctypeDao;
	
	@Inject
	private NamespaceDaoBean namespaceDao;
	
	@Inject
	private XmlResourceDaoBean xmlResourceDao;
	
	@Inject
	private ArchiveEntryDaoBean archiveEntryDao;
	
	@Override
	public void run() {
		for(final ArchiveEntryResource entry : archiveEntryDao.findArchiveEntryWithExtension("xml")) {
			visitArchiveEntry(entry); 
		}
		archiveEntryDao.commit();
	}
	
	@Override
	public void visitArchiveEntry(ArchiveEntryResource entry) {
		//rehydrate to new thread...
		LOG.debug("Processing: "+entry.getArchiveEntry());
		
		//try and read the XML...
		InputStream is = null;
		try {
			is = archiveEntryDao.asInputStream(entry);
			
			Document parsedDocument = LocationAwareXmlReader.readXML(is);
			Doctype docType = (Doctype) parsedDocument.getUserData(LocationAwareContentHandler.DOCTYPE_KEY_NAME);

			//if this is successful, then we know it is a proper XML file.
			//set it to the graph as an XML file.
			XmlResource resource = xmlResourceDao.create(null);
			resource.setResource(entry);
			
			if(docType != null) {
				//create the doctype from
				Iterator<DoctypeMeta> metas = doctypeDao.findByProperties(docType.getName(), docType.getPublicId(), docType.getSystemId(), docType.getBaseURI());
				if(!metas.hasNext()) {
					metas.next().addXmlResource(resource); 
				}
			}
			
			Set<String> namespaces = (Set<String>) parsedDocument.getUserData(LocationAwareContentHandler.NAMESPACE_KEY_NAME);
			if(namespaces != null) {
				for(String namespace : namespaces) {
					NamespaceMeta meta = namespaceDao.createByURI(namespace);
					meta.addXmlResource(resource);
				}
			}
			
		}
		catch(Exception e) {
			LOG.error("Encountered Exception",e);
		} finally {
			IOUtils.closeQuietly(is);
		}
		
	}
}
