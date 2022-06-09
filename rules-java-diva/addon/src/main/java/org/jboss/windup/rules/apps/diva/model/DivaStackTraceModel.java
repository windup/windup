package org.jboss.windup.rules.apps.diva.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;

@TypeValue(DivaStackTraceModel.TYPE)

public interface DivaStackTraceModel extends WindupVertexFrame {
    String TYPE = "DivaStackTraceModel";

    String METHOD = "method";
    String PARENT = "parent";
    String LOCATION = "location";

    @Adjacency(label = METHOD, direction = Direction.OUT)
    JavaMethodModel getMethod();

    @Adjacency(label = METHOD, direction = Direction.OUT)
    void setMethod(JavaMethodModel method);

    @Adjacency(label = PARENT, direction = Direction.OUT)
    DivaStackTraceModel getParent();

    @Adjacency(label = PARENT, direction = Direction.OUT)
    void setParent(DivaStackTraceModel parent);

    @Adjacency(label = LOCATION, direction = Direction.OUT)
    FileLocationModel getLocation();

    @Adjacency(label = LOCATION, direction = Direction.OUT)
    void setLocation(FileLocationModel location);

}
