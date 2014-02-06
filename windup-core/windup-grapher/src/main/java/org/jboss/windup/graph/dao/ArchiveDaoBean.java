package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.Archive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveDaoBean extends BaseDaoBean<Archive> {

	private static Logger LOG = LoggerFactory.getLogger(ArchiveDaoBean.class);
	
	public ArchiveDaoBean() {
		super(Archive.class);
	}
}
