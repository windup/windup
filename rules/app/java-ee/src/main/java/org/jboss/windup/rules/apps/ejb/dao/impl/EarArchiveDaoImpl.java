package org.jboss.windup.rules.apps.ejb.dao.impl;

import javax.inject.Singleton;
import org.jboss.windup.graph.dao.impl.BaseDaoImpl;

import org.jboss.windup.rules.apps.ejb.dao.EarArchiveDao;
import org.jboss.windup.rules.apps.ejb.model.EarArchiveModel;

@Singleton
public class EarArchiveDaoImpl extends BaseDaoImpl<EarArchiveModel> implements EarArchiveDao {

	public EarArchiveDaoImpl() {
		super(EarArchiveModel.class);
	}
	
}
