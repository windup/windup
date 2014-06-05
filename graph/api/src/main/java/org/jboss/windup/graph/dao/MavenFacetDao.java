package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.xml.MavenFacetModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;

public interface MavenFacetDao extends BaseDao<MavenFacetModel> {
	public MavenFacetModel createMaven(String groupId, String artifactId, String version);
	public MavenFacetModel findByGroupArtifactVersion(String groupId, String artifactId, String version);
	public boolean isMavenConfiguration(XmlResourceModel resource);
	public MavenFacetModel getMavenConfigurationFromResource(XmlResourceModel resource);
}
