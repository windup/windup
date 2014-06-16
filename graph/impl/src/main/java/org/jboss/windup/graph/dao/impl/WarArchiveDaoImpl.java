package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.rules.apps.ejb.dao.WarArchiveDao;
import org.jboss.windup.rules.apps.ejb.model.WarArchiveModel;

@Singleton
public class WarArchiveDaoImpl extends BaseDaoImpl<WarArchiveModel> implements WarArchiveDao {

	public WarArchiveDaoImpl() {
		super(WarArchiveModel.class);
	}
	
}
