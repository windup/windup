package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.JarManifestDao;
import org.jboss.windup.graph.model.meta.JarManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JarManifestDaoImpl extends BaseDaoImpl<JarManifest> implements JarManifestDao {

	private static Logger LOG = LoggerFactory.getLogger(JarManifestDaoImpl.class);
	
	public JarManifestDaoImpl() {
		super(JarManifest.class);
	}
}
