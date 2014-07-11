package org.jboss.windup.rules.apps.maven.dao;

import org.jboss.windup.graph.service.Service;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.rules.apps.xml.XmlResourceModel;

public interface MavenModelService extends Service<MavenProjectModel>
{
    public MavenProjectModel createMaven(String groupId, String artifactId, String version);

    public MavenProjectModel findByGroupArtifactVersion(String groupId, String artifactId, String version);

    public boolean isMavenConfiguration(XmlResourceModel resource);

    public MavenProjectModel getMavenConfigurationFromResource(XmlResourceModel resource);
}
