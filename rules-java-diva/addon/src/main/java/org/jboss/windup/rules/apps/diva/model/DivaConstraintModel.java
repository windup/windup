package org.jboss.windup.rules.apps.diva.model;

import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue(DivaConstraintModel.TYPE)
public interface DivaConstraintModel extends WindupVertexFrame {

    String TYPE = "DivaConstraintModel";

    @Adjacency(label = DivaContextModel.CONSTRAINTS, direction = Direction.IN)
    List<DivaContextModel> getContexts();
}
