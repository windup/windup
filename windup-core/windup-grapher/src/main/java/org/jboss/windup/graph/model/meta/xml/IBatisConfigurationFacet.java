package org.jboss.windup.graph.model.meta.xml;

import org.jboss.windup.graph.model.meta.javaclass.IBatisEntityFacet;
import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("IBatisConfigurationFacet")
public interface IBatisConfigurationFacet extends XmlMetaFacet {

	@Label
	@Property("specificationVersion")
	public String getSpecificationVersion();

	@Property("specificationVersion")
	public void setSpecificationVersion(String version);
	
	@Adjacency(label="iBatisEntity", direction=Direction.OUT)
	public Iterable<IBatisEntityFacet> getIBatisEntities();

	@Adjacency(label="iBatisEntity", direction=Direction.OUT)
	public void addIBatisEntityReference(IBatisEntityFacet entity);

}
