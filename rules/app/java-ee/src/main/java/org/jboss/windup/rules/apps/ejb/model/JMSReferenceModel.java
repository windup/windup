package org.jboss.windup.rules.apps.ejb.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue("JMSReference")
public interface JMSReferenceModel extends WindupVertexFrame
{

    @Property("jndiName")
    public String getJndiName();

    @Property("jndiName")
    public void setJndiName(String jndiName);

}
