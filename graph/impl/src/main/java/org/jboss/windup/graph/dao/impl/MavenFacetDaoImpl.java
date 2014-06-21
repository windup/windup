package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.MavenFacetDao;
import org.jboss.windup.rules.apps.maven.model.MavenFacetModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

@Singleton
public class MavenFacetDaoImpl extends BaseDaoImpl<MavenFacetModel> implements MavenFacetDao {

	private static Logger LOG = LoggerFactory.getLogger(MavenFacetDaoImpl.class);
	
	public MavenFacetDaoImpl() {
		super(MavenFacetModel.class);
	}
	
	public MavenFacetModel createMaven(String groupId, String artifactId, String version) {
		MavenFacetModel facet = findByGroupArtifactVersion(groupId, artifactId, version);
		if(facet == null) {
			facet = create();
			facet.setMavenIdentifier(generateMavenKey(groupId, artifactId, version));
			facet.setGroupId(groupId);
			facet.setArtifactId(artifactId);
			facet.setVersion(version);
		}
		
		return facet;
	}
	
	public MavenFacetModel findByGroupArtifactVersion(String groupId, String artifactId, String version) {
		String key = generateMavenKey(groupId, artifactId, version);
		MavenFacetModel facet = this.getByUniqueProperty("mavenIdentifier", key);
		
		return facet;
	}
	
	
	protected String generateMavenKey(String groupId, String artifactId, String version) {
		return groupId+":"+artifactId+":"+version;
	}
	
	public boolean isMavenConfiguration(XmlResourceModel resource) {
        return (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).in("xmlFacet").as("facet").has("type", Text.CONTAINS, this.getTypeValueForSearch()).back("facet").iterator().hasNext();
    }
    
    public MavenFacetModel getMavenConfigurationFromResource(XmlResourceModel resource) {
        @SuppressWarnings("unchecked")
        Iterator<Vertex> v = (Iterator<Vertex>) (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).in("xmlFacet").as("facet").has("type", Text.CONTAINS, this.getTypeValueForSearch()).back("facet").iterator();
        if(v.hasNext()) {
            return getContext().getFramed().frame(v.next(), this.getType());
        }
        
        return null;
    }
}
