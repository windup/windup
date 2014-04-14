package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.ArchiveResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveDao extends BaseDao<ArchiveResource> {

	private static Logger LOG = LoggerFactory.getLogger(ArchiveDao.class);
	
	public ArchiveDao() {
		super(ArchiveResource.class);
	}
}
