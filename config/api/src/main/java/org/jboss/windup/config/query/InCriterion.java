package org.jboss.windup.config.query;


import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import org.jboss.windup.config.GraphRewrite;

/**
 * A Pipes step which gets the vertices behind outgoing edges of given label.
 */
public class InCriterion implements QueryGremlinCriterion
{
    private final String edgeLabel;


    public InCriterion(String edgeLabel)
    {
        this.edgeLabel = edgeLabel;
    }


    @Override
    public void query(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
    {
        pipeline.in(edgeLabel);
    }


    @Override
    public String toString()
    {
        return ".in(" + edgeLabel + ')';
    }

}
