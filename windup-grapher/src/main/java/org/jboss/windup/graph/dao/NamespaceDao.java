package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;

public interface NamespaceDao extends BaseDao<NamespaceMeta> {

	public NamespaceMeta findByURI(String namespaceURI);
}
