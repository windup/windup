package org.jboss.windup.config.query;


import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.config.GraphRewrite;

/**
 * A Pipes step which gets the vertices behind outgoing edges of given label.
 */
public class OutCriterion implements QueryGremlinCriterion {
    private final String edgeLabel;


    public OutCriterion(String edgeLabel) {
        this.edgeLabel = edgeLabel;
    }


    @Override
    public void query(GraphRewrite event, GraphTraversal<?, Vertex> pipeline) {
        pipeline.out(edgeLabel);
    }


    @Override
    public String toString() {
        return ".out(" + edgeLabel + ')';
    }

}
