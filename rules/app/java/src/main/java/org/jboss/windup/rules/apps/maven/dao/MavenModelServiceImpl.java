package org.jboss.windup.rules.apps.maven.dao;

import java.util.Iterator;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.BaseDaoImpl;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.rules.apps.xml.XmlResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

@Singleton
public class MavenModelServiceImpl extends BaseDaoImpl<MavenProjectModel> implements MavenModelService
{

    private static Logger LOG = LoggerFactory.getLogger(MavenModelServiceImpl.class);

    public MavenModelServiceImpl()
    {
        super(MavenProjectModel.class);
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
        MavenProjectModel facet = this.getByUniqueProperty("mavenIdentifier", key);

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
        Iterator<Vertex> v = (Iterator<Vertex>) (new GremlinPipeline<Vertex, Vertex>(resource.asVertex()))
                    .in("xmlFacet").as("facet").has("type", Text.CONTAINS, this.getTypeValueForSearch()).back("facet")
                    .iterator();
        if (v.hasNext())
        {
            return getContext().getFramed().frame(v.next(), this.getType());
        }

        return null;
    }
}
