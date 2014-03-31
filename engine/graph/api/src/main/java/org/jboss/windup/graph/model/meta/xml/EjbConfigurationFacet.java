package org.jboss.windup.graph.model.meta.xml;

import org.jboss.windup.graph.model.meta.javaclass.EjbEntityFacet;
import org.jboss.windup.graph.model.meta.javaclass.EjbSessionBeanFacet;
import org.jboss.windup.graph.model.meta.javaclass.MessageDrivenBeanFacet;
import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("EJBConfigurationFacet")
public interface EjbConfigurationFacet extends XmlMetaFacet {

	@Label
	@Property("specificationVersion")
	public String getSpecificationVersion();

	@Property("specificationVersion")
	public void setSpecificationVersion(String version);
	
	@Adjacency(label="ejbSessionBean", direction=Direction.OUT)
	public Iterable<EjbSessionBeanFacet> getEjbSessionBeans();

	@Adjacency(label="ejbSessionBean", direction=Direction.OUT)
	public void addEjbSessionBean(EjbSessionBeanFacet ejbSessionBean);

	
	@Adjacency(label="ejbEntityBean", direction=Direction.OUT)
	public Iterable<EjbEntityFacet> getEjbEntityBeans();

	@Adjacency(label="ejbSessionBean", direction=Direction.OUT)
	public void addEjbEntityBean(EjbEntityFacet ejbSessionBean);

	
	@Adjacency(label = "ejbEntityBean", direction = Direction.OUT)
	public Iterable<EjbEntityFacet> getEjbEntity();

	@Adjacency(label = "ejbEntityBean", direction = Direction.OUT)
	public void addEjbEntity(EjbEntityFacet ejbEntityBean);
	
	@Adjacency(label = "messageDriven", direction = Direction.OUT)
	public Iterable<MessageDrivenBeanFacet> getMessageDriven();

	@Adjacency(label = "messageDriven", direction = Direction.OUT)
	public void addMessageDriven(MessageDrivenBeanFacet messageDriven);
	
}
