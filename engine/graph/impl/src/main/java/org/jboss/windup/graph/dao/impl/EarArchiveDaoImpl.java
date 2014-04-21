package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.EarArchiveDao;
import org.jboss.windup.graph.model.resource.EarArchive;

@Singleton
public class EarArchiveDaoImpl extends BaseDaoImpl<EarArchive> implements EarArchiveDao {

	public EarArchiveDaoImpl() {
		super(EarArchive.class);
	}
	
}
