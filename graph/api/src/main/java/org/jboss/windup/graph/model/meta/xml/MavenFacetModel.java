package org.jboss.windup.graph.model.meta.xml;

import org.jboss.windup.reporting.renderer.api.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("MavenFacet")
public interface MavenFacetModel extends XmlMetaFacetModel {

	@Label
	@Property("mavenIdentifier")
	public String getMavenIdentifier();

	@Property("mavenIdentifier")
	public void setMavenIdentifier(String identifier);
	
	
	
	@Label
	@Property("specificationVersion")
	public String getSpecificationVersion();

	@Property("specificationVersion")
	public void setSpecificationVersion(String version);
	

	@Property("groupId")
	public String getGroupId();

	@Property("groupId")
	public void setGroupId(String version);

	
	@Property("artifactId")
	public String getArtifactId();

	@Property("artifactId")
	public void setArtifactId(String artifactId);
	

	@Property("version")
	public String getVersion();

	@Property("version")
	public void setVersion(String version);

	
	@Property("name")
	public String getName();

	@Property("name")
	public void setName(String name);


	@Property("url")
	public String getURL();

	@Property("url")
	public void setURL(String url);
	
	
	@Property("description")
	public String getDescription();

	@Property("description")
	public void setDescription(String description);

	
	@Adjacency(label="module", direction=Direction.IN)
	public void setParent(MavenFacetModel maven);
	
	@Adjacency(label="module", direction=Direction.IN)
	public MavenFacetModel getParent();
	
	
	@Adjacency(label="module", direction=Direction.OUT)
	public void addChildModule(MavenFacetModel maven);
	
	@Adjacency(label="module", direction=Direction.OUT)
	public Iterable<MavenFacetModel> getChildModules();

	
	@Adjacency(label="dependency", direction=Direction.OUT)
	public void addDependency(MavenFacetModel maven);
	
	@Adjacency(label="dependency", direction=Direction.OUT)
	public Iterable<MavenFacetModel> getDependencies();

}
