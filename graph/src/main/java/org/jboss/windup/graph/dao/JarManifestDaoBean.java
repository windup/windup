package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.JarManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarManifestDaoBean extends BaseDaoBean<JarManifest> {

	private static Logger LOG = LoggerFactory.getLogger(JarManifestDaoBean.class);
	
	public JarManifestDaoBean() {
		super(JarManifest.class);
	}
}
