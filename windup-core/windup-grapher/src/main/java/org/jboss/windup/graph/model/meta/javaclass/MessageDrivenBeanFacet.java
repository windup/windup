package org.jboss.windup.graph.model.meta.javaclass;

import java.util.Iterator;

import org.jboss.windup.graph.model.meta.Meta;
import org.jboss.windup.graph.model.resource.ArchiveResource;
import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("MessageDrivenBeanFacet")
public interface MessageDrivenBeanFacet extends JavaClassMetaFacet {

	@Label
	@Property("messageDrivenBeanName")
	public String getMessageDrivenBeanName();

	@Property("messageDrivenBeanName")
	public String setMessageDrivenBeanName(String messageDrivenBeanName);

	@Property("displayName")
	public String getDisplayName();

	@Property("displayName")
	public void setDisplayName(String displayName);
	
	@Property("ejbId")
	public String getEjbId();

	@Property("ejbId")
	public void setEjbId(String id);
	
	@Property("transactionType")
	public String getTransactionType();

	@Property("transactionType")
	public void setTransactionType(String transactionType);
	
	@Property("sessionType")
	public String getSessionType();

	@Property("sessionType")
	public void setSessionType(String sessionType);
	
	@Adjacency(label="meta", direction=Direction.OUT)
	public Iterator<Meta> getMeta();
	
	@Adjacency(label="meta", direction=Direction.OUT)
	public void addMeta(final Meta resource);
	
	
}
