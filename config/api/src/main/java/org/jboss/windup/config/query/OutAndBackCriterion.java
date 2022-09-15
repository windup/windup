package org.jboss.windup.config.query;


import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.jboss.windup.config.GraphRewrite;

/**
 * A Pipes step which filters vertices which have outgoing edges of given label.
 */
public class OutAndBackCriterion implements QueryGremlinCriterion {
    private final String edgeLabel;


    public OutAndBackCriterion(String edgeLabel) {
        this.edgeLabel = edgeLabel;
    }


    @Override
    public void query(GraphRewrite event, GraphTraversal<?, Vertex> pipeline) {
        pipeline.as("x").out(edgeLabel).select("x");
    }


    @Override
    public String toString() {
        return ".outAndBack(" + edgeLabel + ')';
    }
}
