package org.jboss.windup.graph.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jboss.windup.engine.util.xml.LocationAwareXmlReader;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.jboss.windup.graph.model.resource.Resource;
import org.jboss.windup.graph.model.resource.XmlResource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XmlResourceDaoBean extends BaseDaoBean<XmlResource> {

	public XmlResourceDaoBean() {
		super(XmlResource.class);
	}

	@Inject
	private ArchiveEntryDaoBean archiveEntryDao;
	
	@Inject
	private NamespaceDaoBean namespaceDao;

	public Iterable<XmlResource> containsNamespaceURI(String namespaceURI) {
		NamespaceMeta namespace = namespaceDao.findByURI(namespaceURI);
		
		//now, check thether it is null.
		if(namespace == null) {
			return new LinkedList<XmlResource>();
		}
		return namespace.getXmlResources();
	}
	
	public Document asDocument(XmlResource resource) throws IOException, SAXException {
		Resource underlyingResource = resource.getResource();
		if(underlyingResource instanceof ArchiveEntryResource) {
			InputStream is = null;
			try {
				is = archiveEntryDao.asInputStream((ArchiveEntryResource)underlyingResource);
				Document parsedDocument = LocationAwareXmlReader.readXML(is);
				return parsedDocument;
			}
			finally {
				IOUtils.closeQuietly(is);
			}
		}

		return null;
	}

}
