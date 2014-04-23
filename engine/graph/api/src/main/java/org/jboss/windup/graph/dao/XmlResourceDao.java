package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.Resource;
import org.jboss.windup.graph.model.resource.XmlResource;

public interface XmlResourceDao extends BaseDao<XmlResource> {
	public Iterable<XmlResource> containsNamespaceURI(String namespaceURI);
	public Iterable<XmlResource> findByRootTag(String rootTagName);
	public boolean isXmlResource(Resource resource);
    public XmlResource getXmlFromResource(Resource resource);
}
