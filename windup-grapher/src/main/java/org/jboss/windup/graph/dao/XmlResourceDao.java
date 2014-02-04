package org.jboss.windup.graph.dao;

import java.util.Iterator;

import org.jboss.windup.graph.model.resource.XmlResource;

public interface XmlResourceDao extends BaseDao<XmlResource> {

	public Iterator<XmlResource> containsNamespaceURI(String namespaceURI);
}
