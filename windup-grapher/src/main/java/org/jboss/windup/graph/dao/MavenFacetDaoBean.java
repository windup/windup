package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.xml.MavenFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenFacetDaoBean extends BaseDaoBean<MavenFacet> {

	private static Logger LOG = LoggerFactory.getLogger(MavenFacetDaoBean.class);
	
	public MavenFacetDaoBean() {
		super(MavenFacet.class);
	}
	
	public MavenFacet createMaven(String groupId, String artifactId, String version) {
		MavenFacet facet = findByGroupArtifactVersion(groupId, artifactId, version);
		if(facet == null) {
			facet = create(null);
			facet.setGroupId(groupId);
			facet.setArtifactId(artifactId);
			facet.setVersion(version);
		}
		
		return facet;
	}
	
	public MavenFacet findByGroupArtifactVersion(String groupId, String artifactId, String version) {
		Iterable<MavenFacet> facets = context.getFramed().query().has("type", typeValue).has("groupId", groupId).has("artifactId", artifactId).has("version", version).vertices(MavenFacet.class);
		if(facets.iterator().hasNext()) {
			return facets.iterator().next();
		}
		
		return null;
	}
	
}
