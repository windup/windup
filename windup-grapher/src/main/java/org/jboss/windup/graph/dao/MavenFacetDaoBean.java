package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.xml.MavenFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenFacetDaoBean extends BaseDaoBean<MavenFacet> {

	private static Logger LOG = LoggerFactory.getLogger(MavenFacetDaoBean.class);
	
	public MavenFacetDaoBean() {
		super(MavenFacet.class);
	}
	
}
