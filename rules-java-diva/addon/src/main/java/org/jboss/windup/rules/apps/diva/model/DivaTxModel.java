package org.jboss.windup.rules.apps.diva.model;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue(DivaTxModel.TYPE)
public interface DivaTxModel extends WindupVertexFrame {

    String TYPE = "DivaTxModel";
    String TXID = "txid";
    String TRANSACTION = "transaction";

    @Property(TXID)
    int getTxid();

    @Property(TXID)
    void setTxid(int txid);

    default List<DivaOpModel> getOps() {
        List<Vertex> vertices = new GraphTraversalSource(getWrappedGraph().getBaseGraph()).V(getElement())
                .out(TRANSACTION).order().by(DivaOpModel.ORDINAL).toList();
        return vertices.stream().map(v -> getGraph().frameElement(v, DivaOpModel.class)).collect(Collectors.toList());
    }

    @Adjacency(label = TRANSACTION, direction = Direction.OUT)
    void addOp(DivaOpModel op);
}
