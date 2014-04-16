package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.WarArchiveDao;
import org.jboss.windup.graph.model.resource.WarArchive;

@Singleton
public class WarArchiveDaoImpl extends BaseDaoImpl<WarArchive> implements WarArchiveDao {

	public WarArchiveDaoImpl() {
		super(WarArchive.class);
	}
	
}
