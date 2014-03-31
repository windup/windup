package org.jboss.windup.graph.dao;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jboss.windup.engine.util.xml.LocationAwareXmlReader;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.jboss.windup.graph.model.resource.Resource;
import org.jboss.windup.graph.model.resource.XmlResource;
import org.w3c.dom.Document;

import com.google.common.collect.Iterables;

public class XmlResourceDao extends BaseDao<XmlResource> {

	public XmlResourceDao() {
		super(XmlResource.class);
	}

	@Inject
	private ArchiveEntryDao archiveEntryDao;
	
	@Inject
	private NamespaceDao namespaceDao;

	public Iterable<XmlResource> containsNamespaceURI(String namespaceURI) {
		
		List<Iterable<XmlResource>> result = new LinkedList<Iterable<XmlResource>>();
		for(NamespaceMeta resource : namespaceDao.findByURI(namespaceURI)) {
			result.add(resource.getXmlResources());
		}
		
		//now, check thether it is null.
		if(result == null || result.size() == 0) {
			return new LinkedList<XmlResource>();
		}
		return Iterables.concat(result);
	}
	
	public Iterable<XmlResource> findByRootTag(String rootTagName) {
		return getByProperty("rootTagName", rootTagName);
	}
	
	
	public Document asDocument(XmlResource resource) throws RuntimeException {
		Resource underlyingResource = resource.getResource();
		if(underlyingResource instanceof ArchiveEntryResource) {
			InputStream is = null;
			try {
				is = archiveEntryDao.asInputStream((ArchiveEntryResource)underlyingResource);
				Document parsedDocument = LocationAwareXmlReader.readXML(is);
				return parsedDocument;
			}
			catch(Exception e) {
				throw new RuntimeException("Exception reading document.", e);
			}
			finally {
				IOUtils.closeQuietly(is);
			}
		}

		return null;
	}

}
