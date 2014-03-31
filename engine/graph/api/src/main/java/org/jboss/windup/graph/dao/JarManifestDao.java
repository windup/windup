package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.JarManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarManifestDao extends BaseDao<JarManifest> {

	private static Logger LOG = LoggerFactory.getLogger(JarManifestDao.class);
	
	public JarManifestDao() {
		super(JarManifest.class);
	}
}
