package org.jboss.windup.rules.apps.maven.dao;

import org.jboss.windup.graph.service.Service;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;

public interface MavenModelService extends Service<MavenProjectModel>
{
    public MavenProjectModel createMavenStub(String groupId, String artifactId, String version);

    public MavenProjectModel findByGroupArtifactVersion(String groupId, String artifactId, String version);
}
