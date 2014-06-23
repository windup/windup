package org.jboss.windup.rules.apps.ejb.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue("JNDIReference")
public interface JNDIReferenceModel extends WindupVertexFrame
{

    @Property("jndiLocation")
    public String getJndiLocation();

    @Property("jndiLocation")
    public void setJndiLocation(String jndiLocation);

}
