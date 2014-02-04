package org.jboss.windup.graph.model.meta.xml;

import java.util.Iterator;

import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("SpringConfigurationFacet")
public interface MavenFacet extends XmlMetaFacet {

	@Label
	@Property("specificationVersion")
	public double getSpecificationVersion();

	@Property("specificationVersion")
	public void getSpecificationVersion(double version);
	

	@Property("groupId")
	public String getGroupId();

	@Property("groupId")
	public void getGroupId(String version);

	
	@Property("artifactId")
	public String getArtifactId();

	@Property("artifactId")
	public void getArtifactId(String artifactId);
	

	@Property("version")
	public String getVersion();

	@Property("version")
	public void getVersion(String version);

	
	@Property("name")
	public String getName();

	@Property("name")
	public void getName(String name);

	
	@Property("description")
	public String getDescription();

	@Property("description")
	public void getDescription(String description);

	
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
