package org.jboss.windup.graph.model.resource.facet.javaclass;

import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("MessageDrivenBeanFacet")
public interface MessageDrivenBeanFacet extends JavaClassMetaFacet {

	@Label
	@Property("messageDrivenBeanName")
	public String getMessageDrivenBeanName();

	@Property("messageDrivenBeanName")
	public String setMessageDrivenBeanName(String messageDrivenBeanName);
	
}
