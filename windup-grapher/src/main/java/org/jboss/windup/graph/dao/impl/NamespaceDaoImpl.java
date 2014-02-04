package org.jboss.windup.graph.dao.impl;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.NamespaceDao;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;

public class NamespaceDaoImpl extends BaseDaoImpl<NamespaceMeta> implements NamespaceDao {

	public NamespaceDaoImpl(GraphContext context) {
		super(context, NamespaceMeta.class);
	}

	@Override
	public NamespaceMeta findByURI(String namespaceURI) {
		return getByUniqueProperty("uri", namespaceURI);
	}

}
