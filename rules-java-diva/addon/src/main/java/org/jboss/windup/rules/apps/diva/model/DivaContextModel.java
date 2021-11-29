package org.jboss.windup.rules.apps.diva.model;

import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue(DivaContextModel.TYPE)
public interface DivaContextModel extends WindupVertexFrame {

    String TYPE = "DivaContextModel";
    String CONSTRAINTS = "constraints";
    String TRANSACTIONS = "transactions";

    @Adjacency(label = CONSTRAINTS, direction = Direction.OUT)
    List<DivaConstraintModel> getConstraints();

    @Adjacency(label = CONSTRAINTS, direction = Direction.OUT)
    void addConstraint(DivaConstraintModel c);

    @Adjacency(label = TRANSACTIONS, direction = Direction.OUT)
    List<DivaTxModel> getTransactions();

    @Adjacency(label = TRANSACTIONS, direction = Direction.OUT)
    void addTransaction(DivaTxModel tx);

}
