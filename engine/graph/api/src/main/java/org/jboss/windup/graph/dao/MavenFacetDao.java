package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.xml.MavenFacet;
import org.jboss.windup.graph.model.resource.XmlResource;

public interface MavenFacetDao extends BaseDao<MavenFacet> {
	public MavenFacet createMaven(String groupId, String artifactId, String version);
	public MavenFacet findByGroupArtifactVersion(String groupId, String artifactId, String version);
	public boolean isMavenConfiguration(XmlResource resource);
	public MavenFacet getMavenConfigurationFromResource(XmlResource resource);
}
