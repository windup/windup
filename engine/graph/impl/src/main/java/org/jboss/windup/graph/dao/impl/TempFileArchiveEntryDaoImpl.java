package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.TempFileArchiveEntryDao;
import org.jboss.windup.graph.model.resource.TempArchiveResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TempFileArchiveEntryDaoImpl extends BaseDaoImpl<TempArchiveResource> implements TempFileArchiveEntryDao {

	private static Logger LOG = LoggerFactory.getLogger(TempFileArchiveEntryDaoImpl.class);
	
	public TempFileArchiveEntryDaoImpl() {
		super(TempArchiveResource.class);
	}
}
