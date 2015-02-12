package org.jboss.windup.rules.apps.maven.dao;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

@Singleton
public class MavenModelService extends GraphService<MavenProjectModel>
{
    @Inject
    public MavenModelService(GraphContext context)
    {
        super(context, MavenProjectModel.class);
    }

    public MavenProjectModel createMavenStub(String groupId, String artifactId, String version)
    {
        MavenProjectModel facet = create();
        facet.setMavenIdentifier(generateMavenKey(groupId, artifactId, version));
        facet.setGroupId(groupId);
        facet.setArtifactId(artifactId);
        facet.setVersion(version);

        return facet;
    }

    public MavenProjectModel findByGroupArtifactVersion(String groupId, String artifactId, String version)
    {
        String key = generateMavenKey(groupId, artifactId, version);
        MavenProjectModel facet = this.getUniqueByProperty(MavenProjectModel.MAVEN_IDENTIFIER, key);

        return facet;
    }

    protected String generateMavenKey(String groupId, String artifactId, String version)
    {
        return groupId + ":" + artifactId + ":" + version;
    }

    public boolean isMavenConfiguration(XmlFileModel resource)
    {
        return (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).in("xmlFacet").as("facet")
                    .has(WindupVertexFrame.TYPE_PROP, this.getTypeValueForSearch()).back("facet")
                    .iterator().hasNext();
    }

    public MavenProjectModel getMavenConfigurationFromResource(XmlFileModel resource)
    {
        @SuppressWarnings("unchecked")
        Iterator<Vertex> v = (Iterator<Vertex>) (new GremlinPipeline<Vertex, Vertex>(resource.asVertex()))
                    .in("xmlFacet").as("facet")
                    .has(WindupVertexFrame.TYPE_PROP, this.getTypeValueForSearch()).back("facet")
                    .iterator();
        if (v.hasNext())
        {
            return getGraphContext().getFramed().frame(v.next(), this.getType());
        }

        return null;
    }

}
