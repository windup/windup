package org.jboss.windup.rules.apps.diva.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;

@TypeValue(DivaOpModel.TYPE)
public interface DivaOpModel extends WindupVertexFrame {

    String TYPE = "DivaOpModel";
    String STACKTRACE = "stacktrace";
    String ORDINAL = "ordinal";
    String METHOD = "method";

    @Property(ORDINAL)
    int getOrdinal();

    @Property(ORDINAL)
    void setOrdinal(int ordinal);

    @Adjacency(label = STACKTRACE, direction = Direction.OUT)
    DivaStackTraceModel getStackTrace();

    @Adjacency(label = STACKTRACE, direction = Direction.OUT)
    void setStackTrace(DivaStackTraceModel model);

    String PARENT = "parent";

    @Adjacency(label = METHOD, direction = Direction.OUT)
    JavaMethodModel getMethod();

    @Adjacency(label = METHOD, direction = Direction.OUT)
    void setMethod(JavaMethodModel method);
}
