package org.jboss.windup.graph.dao.impl;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.windup.graph.dao.ArchiveEntryDao;
import org.jboss.windup.graph.dao.NamespaceDao;
import org.jboss.windup.graph.dao.XmlResourceDao;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;
import org.jboss.windup.graph.model.resource.XmlResource;

import com.google.common.collect.Iterables;

@Singleton
public class XmlResourceDaoImpl extends BaseDaoImpl<XmlResource> implements XmlResourceDao {

	public XmlResourceDaoImpl() {
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

}
