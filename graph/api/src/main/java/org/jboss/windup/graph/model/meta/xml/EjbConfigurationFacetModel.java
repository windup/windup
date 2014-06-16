package org.jboss.windup.graph.model.meta.xml;

import org.jboss.windup.rules.apps.ejb.model.EjbEntityFacetModel;
import org.jboss.windup.rules.apps.ejb.model.EjbSessionBeanFacetModel;
import org.jboss.windup.rules.apps.ejb.model.MessageDrivenBeanFacetModel;
import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("EJBConfigurationFacet")
public interface EjbConfigurationFacetModel extends XmlMetaFacetModel {

	@Label
	@Property("specificationVersion")
	public String getSpecificationVersion();

	@Property("specificationVersion")
	public void setSpecificationVersion(String version);
	
	@Adjacency(label="ejbSessionBean", direction=Direction.OUT)
	public Iterable<EjbSessionBeanFacetModel> getEjbSessionBeans();

	@Adjacency(label="ejbSessionBean", direction=Direction.OUT)
	public void addEjbSessionBean(EjbSessionBeanFacetModel ejbSessionBean);

	
	@Adjacency(label="ejbEntityBean", direction=Direction.OUT)
	public Iterable<EjbEntityFacetModel> getEjbEntityBeans();

	@Adjacency(label="ejbSessionBean", direction=Direction.OUT)
	public void addEjbEntityBean(EjbEntityFacetModel ejbSessionBean);

	
	@Adjacency(label = "ejbEntityBean", direction = Direction.OUT)
	public Iterable<EjbEntityFacetModel> getEjbEntity();

	@Adjacency(label = "ejbEntityBean", direction = Direction.OUT)
	public void addEjbEntity(EjbEntityFacetModel ejbEntityBean);
	
	@Adjacency(label = "messageDriven", direction = Direction.OUT)
	public Iterable<MessageDrivenBeanFacetModel> getMessageDriven();

	@Adjacency(label = "messageDriven", direction = Direction.OUT)
	public void addMessageDriven(MessageDrivenBeanFacetModel messageDriven);
	
}
