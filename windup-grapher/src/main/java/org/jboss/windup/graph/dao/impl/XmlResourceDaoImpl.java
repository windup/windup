package org.jboss.windup.graph.dao.impl;

import javax.inject.Inject;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.DoctypeDao;
import org.jboss.windup.graph.dao.NamespaceDao;
import org.jboss.windup.graph.dao.XmlResourceDao;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;
import org.jboss.windup.graph.model.resource.XmlResource;

import com.thinkaurelius.titan.util.datastructures.IterablesUtil;

public class XmlResourceDaoImpl extends BaseDaoImpl<XmlResource> implements XmlResourceDao {

	@Inject
	private DoctypeDao doctypeDao;
	
	@Inject
	private NamespaceDao namespaceDao;
	
	public XmlResourceDaoImpl(GraphContext context) {
		super(context, XmlResource.class);
	}

	@Override
	public Iterable<XmlResource> containsNamespaceURI(String namespaceURI) {
		NamespaceMeta namespace = namespaceDao.findByURI(namespaceURI);
		
		//now, check thether it is null.
		if(namespace == null) {
			return IterablesUtil.emptyIterable();
		}
		return namespace.getXmlResources();
	}

}
