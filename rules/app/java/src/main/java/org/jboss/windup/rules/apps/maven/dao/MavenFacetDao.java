package org.jboss.windup.rules.apps.maven.dao;

import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.rules.apps.maven.model.MavenProjectModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;

public interface MavenFacetDao extends BaseDao<MavenProjectModel> {
	public MavenProjectModel createMaven(String groupId, String artifactId, String version);
	public MavenProjectModel findByGroupArtifactVersion(String groupId, String artifactId, String version);
	public boolean isMavenConfiguration(XmlResourceModel resource);
	public MavenProjectModel getMavenConfigurationFromResource(XmlResourceModel resource);
}
