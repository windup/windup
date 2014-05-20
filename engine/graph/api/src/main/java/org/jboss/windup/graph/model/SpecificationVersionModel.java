package org.jboss.windup.graph.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("SpecificationVersion")
public interface SpecificationVersionModel extends WindupVertexFrame
{

    @Property("specVersion")
    public String getSpecVersion();

    @Property("specVersion")
    public void setSpecVersion(String version);

}
