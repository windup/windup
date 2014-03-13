package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.xml.MavenFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenFacetDao extends BaseDao<MavenFacet> {

	private static Logger LOG = LoggerFactory.getLogger(MavenFacetDao.class);
	
	public MavenFacetDao() {
		super(MavenFacet.class);
	}
	
	public MavenFacet createMaven(String groupId, String artifactId, String version) {
		MavenFacet facet = findByGroupArtifactVersion(groupId, artifactId, version);
		if(facet == null) {
			facet = create(null);
			facet.setMavenIdentifier(generateMavenKey(groupId, artifactId, version));
			facet.setGroupId(groupId);
			facet.setArtifactId(artifactId);
			facet.setVersion(version);
		}
		
		return facet;
	}
	
	public MavenFacet findByGroupArtifactVersion(String groupId, String artifactId, String version) {
		String key = generateMavenKey(groupId, artifactId, version);
		MavenFacet facet = this.getByUniqueProperty("mavenIdentifier", key);
		
		return facet;
	}
	
	
	protected String generateMavenKey(String groupId, String artifactId, String version) {
		return groupId+":"+artifactId+":"+version;
	}
}
