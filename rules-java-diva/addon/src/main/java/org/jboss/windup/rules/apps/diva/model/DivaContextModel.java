package org.jboss.windup.rules.apps.diva.model;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
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

    default List<DivaTxModel> getTransactions() {
        List<Vertex> vertices = new GraphTraversalSource(getWrappedGraph().getBaseGraph()).V(getElement())
                .out(TRANSACTIONS).order().by(DivaTxModel.TXID).toList();
        return vertices.stream().map(v -> getGraph().frameElement(v, DivaTxModel.class)).collect(Collectors.toList());
    }

    @Adjacency(label = TRANSACTIONS, direction = Direction.OUT)
    void addTransaction(DivaTxModel tx);

}
