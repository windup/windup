package org.jboss.windup.graph.model.meta.javaclass;

import java.util.Iterator;

import org.jboss.windup.graph.model.meta.Meta;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("EJBEntityFacet")
public interface EjbEntityFacet extends JavaClassMetaFacet {

	@Label
	@Property("ejbEntityName")
	public String getEjbEntityName();

	@Property("ejbEntityName")
	public String setEjbEntityName(String ejbEntityName);
	
	@Property("displayName")
	public String getDisplayName();

	@Property("displayName")
	public void setDisplayName(String displayName);
	
	@Property("ejbId")
	public String getEjbId();

	@Property("ejbId")
	public void setEjbId(String id);
	
	@Property("persistenceType")
	public String getPersistenceType();

	@Property("persistenceType")
	public void setPersistenceType(String PersistenceType);
	
	@Adjacency(label = "ejbLocal", direction = Direction.OUT)
	public void setEjbLocal(JavaClass ejbLocal);

	@Adjacency(label = "ejbLocal", direction = Direction.OUT)
	public JavaClass getEjbLocal();
	
	@Adjacency(label = "ejbLocalHome", direction = Direction.OUT)
	public void setEjbLocalHome(JavaClass ejbLocalHome);

	@Adjacency(label = "ejbLocalHome", direction = Direction.OUT)
	public JavaClass getEjbLocalHome();
	

	@Adjacency(label="meta", direction=Direction.OUT)
	public Iterator<Meta> getMeta();
	
	@Adjacency(label="meta", direction=Direction.OUT)
	public void addMeta(final Meta resource);
}
