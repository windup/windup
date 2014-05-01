package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.WarArchiveDao;
import org.jboss.windup.graph.model.resource.WarArchiveModel;

@Singleton
public class WarArchiveDaoImpl extends BaseDaoImpl<WarArchiveModel> implements WarArchiveDao {

	public WarArchiveDaoImpl() {
		super(WarArchiveModel.class);
	}
	
}
