package org.jboss.windup.rules.apps.diva.model;

import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Direction;
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

    @Adjacency(label = TRANSACTION, direction = Direction.OUT)
    List<DivaOpModel> getOps();

    @Adjacency(label = TRANSACTION, direction = Direction.OUT)
    void addOp(DivaOpModel op);
}
