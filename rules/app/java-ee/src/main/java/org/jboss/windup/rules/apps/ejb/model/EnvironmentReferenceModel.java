package org.jboss.windup.rules.apps.ejb.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue("EnvironmentReference")
public interface EnvironmentReferenceModel extends WindupVertexFrame
{

    @Property("referenceId")
    public String getReferenceId();

    @Property("referenceId")
    public void setReferenceId(String resourceId);

    @Property("name")
    public String getName();

    @Property("name")
    public void setName(String name);

    @Property("referenceType")
    public String getReferenceType();

    @Property("referenceType")
    public void setReferenceType(String referenceType);
}
