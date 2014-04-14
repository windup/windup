package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.TempArchiveResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TempFileArchiveEntryDao extends BaseDao<TempArchiveResource> {

	private static Logger LOG = LoggerFactory.getLogger(TempFileArchiveEntryDao.class);
	
	public TempFileArchiveEntryDao() {
		super(TempArchiveResource.class);
	}
}
