package org.jboss.windup.graph.model.meta.xml;

import java.util.Iterator;

import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("MavenFacet")
public interface MavenFacet extends XmlMetaFacet {

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
	public void setParent(MavenFacet maven);
	
	@Adjacency(label="module", direction=Direction.IN)
	public MavenFacet getParent();
	
	
	@Adjacency(label="module", direction=Direction.OUT)
	public void addChildModule(MavenFacet maven);
	
	@Adjacency(label="module", direction=Direction.OUT)
	public MavenFacet getChildModules();
	
	
	@Adjacency(label="dependency", direction=Direction.OUT)
	public void addDependency(MavenFacet maven);
	
	@Adjacency(label="dependency", direction=Direction.OUT)
	public Iterator<MavenFacet> getDependencies();

}
