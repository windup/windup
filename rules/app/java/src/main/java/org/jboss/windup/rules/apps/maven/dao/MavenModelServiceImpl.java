package org.jboss.windup.rules.apps.maven.dao;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.rules.apps.xml.XmlResourceModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

@Singleton
public class MavenModelServiceImpl extends GraphService<MavenProjectModel> implements MavenModelService
{

    @Inject
    public MavenModelServiceImpl(GraphContext context)
    {
        super(context, MavenProjectModel.class);
    }

    public MavenProjectModel createMaven(String groupId, String artifactId, String version)
    {
        MavenProjectModel facet = findByGroupArtifactVersion(groupId, artifactId, version);
        if (facet == null)
        {
            facet = create();
            facet.setMavenIdentifier(generateMavenKey(groupId, artifactId, version));
            facet.setGroupId(groupId);
            facet.setArtifactId(artifactId);
            facet.setVersion(version);
        }

        return facet;
    }

    public MavenProjectModel findByGroupArtifactVersion(String groupId, String artifactId, String version)
    {
        String key = generateMavenKey(groupId, artifactId, version);
        MavenProjectModel facet = this.getUniqueByProperty("mavenIdentifier", key);
        return facet;
    }

    protected String generateMavenKey(String groupId, String artifactId, String version)
    {
        return groupId + ":" + artifactId + ":" + version;
    }

    public boolean isMavenConfiguration(XmlResourceModel resource)
    {
        return (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).in("xmlFacet").as("facet")
                    .has("type", Text.CONTAINS, this.getTypeValueForSearch()).back("facet").iterator().hasNext();
    }

    public MavenProjectModel getMavenConfigurationFromResource(XmlResourceModel resource)
    {
        @SuppressWarnings("unchecked")
        Iterator<Vertex> vertices = (Iterator<Vertex>) (new GremlinPipeline<Vertex, Vertex>(resource.asVertex()))
                    .in("xmlFacet").as("facet").has("type", Text.CONTAINS, this.getTypeValueForSearch()).back("facet")
                    .iterator();
        if (vertices.hasNext())
        {
            Vertex vertex = vertices.next();
            return frame(vertex);
        }

        return null;
    }

}
