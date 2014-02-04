package org.jboss.windup.graph.dao;

import javax.inject.Inject;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;
import org.jboss.windup.graph.model.resource.XmlResource;

import com.thinkaurelius.titan.util.datastructures.IterablesUtil;

public class XmlResourceDaoBean extends BaseDaoBean<XmlResource> {

	@Inject
	private DoctypeDaoBean doctypeDao;
	
	@Inject
	private NamespaceDaoBean namespaceDao;
	
	public XmlResourceDaoBean(GraphContext context) {
		super(context, XmlResource.class);
	}

	public Iterable<XmlResource> containsNamespaceURI(String namespaceURI) {
		NamespaceMeta namespace = namespaceDao.findByURI(namespaceURI);
		
		//now, check thether it is null.
		if(namespace == null) {
			return IterablesUtil.emptyIterable();
		}
		return namespace.getXmlResources();
	}

}
