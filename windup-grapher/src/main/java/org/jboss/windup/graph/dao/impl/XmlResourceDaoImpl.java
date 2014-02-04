package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import javax.inject.Inject;

import org.apache.commons.collections.IteratorUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.DoctypeDao;
import org.jboss.windup.graph.dao.NamespaceDao;
import org.jboss.windup.graph.dao.XmlResourceDao;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;
import org.jboss.windup.graph.model.resource.XmlResource;

public class XmlResourceDaoImpl extends BaseDaoImpl<XmlResource> implements XmlResourceDao {

	@Inject
	private DoctypeDao doctypeDao;
	
	@Inject
	private NamespaceDao namespaceDao;
	
	public XmlResourceDaoImpl(GraphContext context) {
		super(context, XmlResource.class);
	}

	@Override
	public Iterator<XmlResource> containsNamespaceURI(String namespaceURI) {
		NamespaceMeta namespace = namespaceDao.findByURI(namespaceURI);
		
		//now, check thether it is null.
		if(namespace == null) {
			return IteratorUtils.emptyIterator();
		}
		return namespace.getXmlResources();
	}

}
