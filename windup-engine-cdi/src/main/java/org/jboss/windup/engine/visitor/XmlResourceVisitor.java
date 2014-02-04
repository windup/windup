package org.jboss.windup.engine.visitor;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jboss.windup.engine.WindupContext;
import org.jboss.windup.engine.util.xml.LocationAwareContentHandler;
import org.jboss.windup.engine.util.xml.LocationAwareContentHandler.Doctype;
import org.jboss.windup.engine.util.xml.LocationAwareXmlReader;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.ArchiveEntryDao;
import org.jboss.windup.graph.dao.DoctypeDao;
import org.jboss.windup.graph.dao.NamespaceDao;
import org.jboss.windup.graph.dao.XmlResourceDao;
import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.jboss.windup.graph.model.resource.XmlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class XmlResourceVisitor extends EmptyGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(XmlResourceVisitor.class);
	
	@Inject
	private DoctypeDao doctypeDao;
	
	@Inject
	private NamespaceDao namespaceDao;
	
	@Inject
	private XmlResourceDao xmlResourceDao;
	
	@Inject
	private ArchiveEntryDao archiveEntryDao;
	
	@Override
	public void visitContext(WindupContext context) {
		for(ArchiveEntryResource entry : archiveEntryDao.findArchiveEntryWithExtension("xml")) {
			visitArchiveEntry(entry);
		}
	}
	
	@Override
	public void visitArchiveEntry(ArchiveEntryResource entry) {
		LOG.info("Processing: "+entry.getArchiveEntry());
		
		//try and read the XML...
		ZipFile zipFile = null;
		InputStream is = null;
		try {
			zipFile = new ZipFile(new File(entry.getArchive().getFilePath()));
			ZipEntry zipEntry = zipFile.getEntry(entry.getArchiveEntry());
			is = zipFile.getInputStream(zipEntry);
			
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
					NamespaceMeta meta = namespaceDao.findByURI(namespace);
					if(meta == null) {
						LOG.info("Adding namespace: "+namespace);
						//create the namespace...
						meta = namespaceDao.create(null);
						meta.addXmlResource(resource);
					}
					else {
						meta.addXmlResource(resource);
					}
				}
			}
			
		}
		catch(Exception e) {
			
		} finally {
			IOUtils.closeQuietly(zipFile);
			IOUtils.closeQuietly(is);
		}
		
	}
}
