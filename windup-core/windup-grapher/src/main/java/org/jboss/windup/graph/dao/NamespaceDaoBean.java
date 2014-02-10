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

	public NamespaceMeta findByURI(String namespaceURI) {
		return getByUniqueProperty("namespaceURI", namespaceURI);
	}
	
	public synchronized NamespaceMeta createByURI(String namespaceURI) {
		NamespaceMeta meta = getByUniqueProperty("namespaceURI", namespaceURI);
		if(meta == null) {
			LOG.info("Adding namespace: "+namespaceURI);
			meta = this.create(null);
			meta.setURI(namespaceURI);
		}
		
		return meta;
	}

}
