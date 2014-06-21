package org.jboss.windup.rules.apps.xml.dao;

import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.graph.model.meta.xml.NamespaceMetaModel;

public interface NamespaceDao extends BaseDao<NamespaceMetaModel> {
	public Iterable<NamespaceMetaModel> findByURI(String namespaceURI);
	public Iterable<NamespaceMetaModel> findByURIs(String ... uriRegex);
	public Iterable<NamespaceMetaModel> findSchemaLocationRegexMatch(String schemaLocationRegex);
	public NamespaceMetaModel createNamespaceSchemaLocation(String namespaceURI, String schemaLocation);
}
