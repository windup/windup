package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;

public class NamespaceDaoBean extends BaseDaoBean<NamespaceMeta> {

	public NamespaceDaoBean() {
		super(NamespaceMeta.class);
	}

	public NamespaceMeta findByURI(String namespaceURI) {
		return getByUniqueProperty("namespaceURI", namespaceURI);
	}

}
