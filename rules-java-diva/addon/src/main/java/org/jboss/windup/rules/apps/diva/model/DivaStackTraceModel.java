package org.jboss.windup.rules.apps.diva.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.IndexType;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;

@TypeValue(DivaStackTraceModel.TYPE)

public interface DivaStackTraceModel extends FileLocationModel {
    String TYPE = "DivaStackTraceModel";

    String METHOD = "method";
    String PARENT = "parent";
    String LOCATION = "parent";

    @Override
    @Indexed(value = IndexType.DEFAULT, dataType = Integer.class)
    @Property(LINE_NUMBER)
    int getLineNumber();

    @Override
    @Indexed(value = IndexType.DEFAULT, dataType = Integer.class)
    @Property(COLUMN_NUMBER)
    int getColumnNumber();

    @Override
    @Indexed(value = IndexType.DEFAULT, dataType = Integer.class)
    @Property(LENGTH)
    int getLength();

    @Adjacency(label = METHOD, direction = Direction.OUT)
    JavaMethodModel getMethod();

    @Adjacency(label = METHOD, direction = Direction.OUT)
    void setMethod(JavaMethodModel method);

    @Adjacency(label = PARENT, direction = Direction.OUT)
    DivaStackTraceModel getParent();

    @Adjacency(label = PARENT, direction = Direction.OUT)
    void setParent(DivaStackTraceModel parent);

}
