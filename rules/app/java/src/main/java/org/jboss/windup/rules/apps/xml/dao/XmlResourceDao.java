package org.jboss.windup.rules.apps.xml.dao;

import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;

public interface XmlResourceDao extends BaseDao<XmlResourceModel> {
	public Iterable<XmlResourceModel> containsNamespaceURI(String namespaceURI);
	public Iterable<XmlResourceModel> findByRootTag(String rootTagName);
	public boolean isXmlResource(ResourceModel resource);
    public XmlResourceModel getXmlFromResource(ResourceModel resource);
}
