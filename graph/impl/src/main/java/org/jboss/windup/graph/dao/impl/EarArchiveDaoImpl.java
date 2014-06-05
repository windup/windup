package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.EarArchiveDao;
import org.jboss.windup.graph.model.resource.EarArchiveModel;

@Singleton
public class EarArchiveDaoImpl extends BaseDaoImpl<EarArchiveModel> implements EarArchiveDao {

	public EarArchiveDaoImpl() {
		super(EarArchiveModel.class);
	}
	
}
