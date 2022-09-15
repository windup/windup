package org.jboss.windup.rules.apps.maven.dao;

import java.util.Iterator;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Contains methods for searching and deleting {@link MavenProjectModel}s.
 */
public class MavenProjectService extends GraphService<MavenProjectModel> {
    public MavenProjectService(GraphContext context) {
        super(context, MavenProjectModel.class);
    }

    public MavenProjectModel createMavenStub(String groupId, String artifactId, String version) {
        MavenProjectModel facet = create();
        facet.setMavenIdentifier(generateMavenKey(groupId, artifactId, version));
        facet.setGroupId(groupId);
        facet.setArtifactId(artifactId);
        facet.setVersion(version);

        return facet;
    }

    /**
     * Find all {@link MavenProjectModel}s that match the given groupId, artifactId, and version. Note that this could potentially return multiple
     * projects if multiple projects are in the original application with the same GAV.
     */
    public Iterable<MavenProjectModel> findByGroupArtifactVersion(String groupId, String artifactId, String version) {
        String key = generateMavenKey(groupId, artifactId, version);
        Iterable<MavenProjectModel> facet = this.findAllByProperty(MavenProjectModel.MAVEN_IDENTIFIER, key);

        return facet;
    }

    protected String generateMavenKey(String groupId, String artifactId, String version) {
        return groupId + ":" + artifactId + ":" + version;
    }

    public boolean isMavenConfiguration(XmlFileModel resource) {
        return new GraphTraversalSource(this.getGraphContext().getGraph())
                .V(resource)
                .in("xmlFacet")
                .as("facet")
                .has(WindupVertexFrame.TYPE_PROP, GraphTypeManager.getTypeValue(this.getType()))
                .select("facet")
                .hasNext();
    }

    public MavenProjectModel getMavenConfigurationFromResource(XmlFileModel resource) {
        Iterator<Vertex> v = new GraphTraversalSource(this.getGraphContext().getGraph())
                .V(resource)
                .in("xmlFacet").as("facet")
                .has(WindupVertexFrame.TYPE_PROP, GraphTypeManager.getTypeValue(this.getType()))
                .select("facet");
        if (v.hasNext()) {
            return getGraphContext().getFramed().frameElement(v.next(), this.getType());
        }

        return null;
    }

}
