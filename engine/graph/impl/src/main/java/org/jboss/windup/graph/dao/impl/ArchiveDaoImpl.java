package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.ArchiveDao;
import org.jboss.windup.graph.model.resource.ArchiveResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ArchiveDaoImpl extends BaseDaoImpl<ArchiveResource> implements ArchiveDao {

	private static Logger LOG = LoggerFactory.getLogger(ArchiveDaoImpl.class);
	
	public ArchiveDaoImpl() {
		super(ArchiveResource.class);
	}
}
