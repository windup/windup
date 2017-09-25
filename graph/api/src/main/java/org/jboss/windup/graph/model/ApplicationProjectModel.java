package org.jboss.windup.graph.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(ApplicationProjectModel.TYPE)
public interface ApplicationProjectModel extends ProjectModel
{
    String TYPE = "ApplicationProjectModel";
    public static final String PROPERTY_APPLICATION_NAME = "applicationName";

    @Property(PROPERTY_APPLICATION_NAME)
    public void setApplicationName(String name);

    @Property(PROPERTY_APPLICATION_NAME)
    public String getApplicationName();
}
