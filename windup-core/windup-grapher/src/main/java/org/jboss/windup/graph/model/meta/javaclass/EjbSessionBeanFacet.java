package org.jboss.windup.graph.model.meta.javaclass;

import java.util.Iterator;

import org.jboss.windup.graph.model.meta.Meta;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("EJBSessionBean")
public interface EjbSessionBeanFacet extends JavaClassMetaFacet {

	@Label
	@Property("ejbSessionBeanName")
	public String getSessionBeanName();

	@Property("ejbSessionBeanName")
	public void setSessionBeanName(String ejbSessionBeanName);

	@Property("sessionType")
	public String getSessionType();

	@Property("sessionType")
	public void setSessionType(String sessionType);
	
	@Property("transactionType")
	public String getTransactionType();

	@Property("transactionType")
	public void setTransactionType(String transactionType);
	
	@Property("displayName")
	public String getDisplayName();

	@Property("displayName")
	public void setDisplayName(String displayName);
	
	@Property("ejbId")
	public String getEjbId();

	@Property("ejbId")
	public void setEjbId(String id);
	
	@Adjacency(label = "ejbLocal", direction = Direction.OUT)
	public void setEjbLocal(JavaClass ejbLocal);

	@Adjacency(label = "ejbLocal", direction = Direction.OUT)
	public JavaClass getEjbLocal();
	
	@Adjacency(label = "ejbRemote", direction = Direction.OUT)
	public void setEjbRemote(JavaClass ejbRemote);

	@Adjacency(label = "ejbRemote", direction = Direction.OUT)
	public JavaClass getEjbRemote();
	
	@Adjacency(label = "ejbLocalHome", direction = Direction.OUT)
	public void setEjbLocalHome(JavaClass ejbLocalHome);

	@Adjacency(label = "ejbLocalHome", direction = Direction.OUT)
	public JavaClass getEjbLocalHome();
	
	@Adjacency(label = "ejbHome", direction = Direction.OUT)
	public void setEjbHome(JavaClass ejbHome);

	@Adjacency(label = "ejbHome", direction = Direction.OUT)
	public JavaClass getEjbHome();
	

	@Adjacency(label="meta", direction=Direction.OUT)
	public Iterator<Meta> getMeta();
	
	@Adjacency(label="meta", direction=Direction.OUT)
	public void addMeta(final Meta resource);
}
