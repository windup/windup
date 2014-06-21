package org.jboss.windup.rules.apps.xml.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.rules.apps.xml.dao.NamespaceDao;
import org.jboss.windup.graph.model.meta.xml.NamespaceMetaModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.core.attribute.Text;
import org.jboss.windup.graph.dao.impl.BaseDaoImpl;

@Singleton
public class NamespaceDaoImpl extends BaseDaoImpl<NamespaceMetaModel> implements NamespaceDao {
	private static final Logger LOG = LoggerFactory.getLogger(NamespaceDaoImpl.class);
	
	public NamespaceDaoImpl() {
		super(NamespaceMetaModel.class);
	}

	public Iterable<NamespaceMetaModel> findByURI(String namespaceURI) {
		return getByProperty("namespaceURI", namespaceURI);
	}
	
	public Iterable<NamespaceMetaModel> findByURIs(String ... uriRegex) {
		return super.findValueMatchingRegex("namespaceURI", uriRegex);
	}
	
	public Iterable<NamespaceMetaModel> findSchemaLocationRegexMatch(String schemaLocationRegex) {
		return super.findValueMatchingRegex("schemaLocation", schemaLocationRegex);
	}
	
	public NamespaceMetaModel createNamespaceSchemaLocation(String namespaceURI, String schemaLocation) {
		Iterable<NamespaceMetaModel> results = getContext().getFramed().query().has("type", Text.CONTAINS, getTypeValueForSearch()).has("namespaceURI", namespaceURI).has("schemaLocation", schemaLocation).vertices(getType());
		
		for(NamespaceMetaModel result : results) {
			return result;
		}
		
		//otherwise, create it.
		NamespaceMetaModel meta = this.create();
		meta.setSchemaLocation(schemaLocation);
		meta.setURI(namespaceURI);
		
		return meta;
	}

}
