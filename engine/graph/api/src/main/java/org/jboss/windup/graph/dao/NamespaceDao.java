package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;

public interface NamespaceDao extends BaseDao<NamespaceMeta> {
	public Iterable<NamespaceMeta> findByURI(String namespaceURI);
	public Iterable<NamespaceMeta> findByURIs(String ... uriRegex);
	public Iterable<NamespaceMeta> findSchemaLocationRegexMatch(String schemaLocationRegex);
	public NamespaceMeta createNamespaceSchemaLocation(String namespaceURI, String schemaLocation);
}
