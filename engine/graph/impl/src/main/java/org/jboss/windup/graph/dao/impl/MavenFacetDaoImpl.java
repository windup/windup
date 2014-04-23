package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.MavenFacetDao;
import org.jboss.windup.graph.model.meta.xml.MavenFacet;
import org.jboss.windup.graph.model.resource.XmlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

@Singleton
public class MavenFacetDaoImpl extends BaseDaoImpl<MavenFacet> implements MavenFacetDao {

	private static Logger LOG = LoggerFactory.getLogger(MavenFacetDaoImpl.class);
	
	public MavenFacetDaoImpl() {
		super(MavenFacet.class);
	}
	
	public MavenFacet createMaven(String groupId, String artifactId, String version) {
		MavenFacet facet = findByGroupArtifactVersion(groupId, artifactId, version);
		if(facet == null) {
			facet = create();
			facet.setMavenIdentifier(generateMavenKey(groupId, artifactId, version));
			facet.setGroupId(groupId);
			facet.setArtifactId(artifactId);
			facet.setVersion(version);
		}
		
		return facet;
	}
	
	public MavenFacet findByGroupArtifactVersion(String groupId, String artifactId, String version) {
		String key = generateMavenKey(groupId, artifactId, version);
		MavenFacet facet = this.getByUniqueProperty("mavenIdentifier", key);
		
		return facet;
	}
	
	
	protected String generateMavenKey(String groupId, String artifactId, String version) {
		return groupId+":"+artifactId+":"+version;
	}
	
	public boolean isMavenConfiguration(XmlResource resource) {
        return (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).in("xmlFacet").as("facet").has("type", this.typeValue).back("facet").iterator().hasNext();
    }
    
    public MavenFacet getMavenConfigurationFromResource(XmlResource resource) {
        Iterator<Vertex> v = (Iterator<Vertex>) (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).in("xmlFacet").as("facet").has("type", this.typeValue).back("facet").iterator();
        if(v.hasNext()) {
            return context.getFramed().frame(v.next(), this.type);
        }
        
        return null;
    }
}
