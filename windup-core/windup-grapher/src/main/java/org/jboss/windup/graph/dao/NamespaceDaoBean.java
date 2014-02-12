package org.jboss.windup.graph.dao;

import javax.inject.Singleton;

import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NamespaceDaoBean extends BaseDaoBean<NamespaceMeta> {
	private static final Logger LOG = LoggerFactory.getLogger(NamespaceDaoBean.class);
	public NamespaceDaoBean() {
		super(NamespaceMeta.class);
	}

	public Iterable<NamespaceMeta> findByURI(String namespaceURI) {
		return getByProperty("namespaceURI", namespaceURI);
	}
	
	public Iterable<NamespaceMeta> findByURIs(String ... uriRegex) {
		return super.findValueMatchingRegex("namespaceURI", uriRegex);
	}
	
	public Iterable<NamespaceMeta> findSchemaLocationRegexMatch(String schemaLocationRegex) {
		return super.findValueMatchingRegex("schemaLocation", schemaLocationRegex);
	}
	
	public NamespaceMeta createNamespaceSchemaLocation(String namespaceURI, String schemaLocation) {
		Iterable<NamespaceMeta> results = getContext().getFramed().query().has("type", typeValue).has("namespaceURI", namespaceURI).has("schemaLocation", schemaLocation).vertices(type);
		
		for(NamespaceMeta result : results) {
			return result;
		}
		
		//otherwise, create it.
		NamespaceMeta meta = this.create();
		meta.setSchemaLocation(schemaLocation);
		meta.setURI(namespaceURI);
		
		return meta;
	}

}
