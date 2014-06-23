package org.jboss.windup.rules.apps.ejb.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue("SpecificationVersion")
public interface SpecificationVersionModel extends WindupVertexFrame
{

    @Property("specVersion")
    public String getSpecVersion();

    @Property("specVersion")
    public void setSpecVersion(String version);

}
