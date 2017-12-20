package org.jboss.windup.graph.model;

import com.syncleus.ferma.annotations.Property;

@TypeValue(ApplicationModel.TYPE)
public interface ApplicationModel extends WindupVertexFrame
{
    String TYPE = "ApplicationModel";
    String PROPERTY_APPLICATION_NAME = "applicationName";

    @Property(PROPERTY_APPLICATION_NAME)
    void setApplicationName(String name);

    @Property(PROPERTY_APPLICATION_NAME)
    String getApplicationName();
}
