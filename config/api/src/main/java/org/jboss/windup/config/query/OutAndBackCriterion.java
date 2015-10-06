package org.jboss.windup.config.query;


import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import org.jboss.windup.config.GraphRewrite;

/**
 * A Pipes step which filters vertices which have outgoing edges of given label.
 */
public class OutAndBackCriterion implements QueryGremlinCriterion
{
    private final String edgeLabel;


    public OutAndBackCriterion(String edgeLabel)
    {
        this.edgeLabel = edgeLabel;
    }


    @Override
    public void query(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
    {
        pipeline.as("x").out(edgeLabel).back("x");
    }


    @Override
    public String toString()
    {
        return ".outAndBack(" + edgeLabel + ')';
    }
}
