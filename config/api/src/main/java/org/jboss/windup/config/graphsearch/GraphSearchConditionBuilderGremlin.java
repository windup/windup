package org.jboss.windup.config.graphsearch;

import java.util.HashSet;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.runner.VarStack;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Provides access to the full GremlinPipeline API:
 * <ul>
 * <li><a href="https://github.com/tinkerpop/gremlin/wiki">Gremlin Wiki</a></li>
 * <li><a href="https://github.com/tinkerpop/gremlin/wiki/Gremlin-Steps">Gremlin Steps (Cheatsheet)</a></li>
 * <li><a
 * href="http://www.tinkerpop.com/docs/javadocs/gremlin/2.4.0/com/tinkerpop/gremlin/java/GremlinPipeline.html">Gremlin
 * Pipeline Javadoc</a></li>
 * </ul>
 * 
 * @author jsightler
 * 
 */
public class GraphSearchConditionBuilderGremlin extends GraphCondition
{
    private String variableName;
    private GraphSearchConditionBuilder graphSearchConditionBuilder;
    private Iterable<Vertex> initialVertices;

    private GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>();

    public GraphSearchConditionBuilderGremlin()
    {

    }

    public GraphSearchConditionBuilderGremlin(GraphSearchConditionBuilder graphSearchConditionBuilder)
    {
        this.variableName = graphSearchConditionBuilder.getVariableName();
        this.graphSearchConditionBuilder = graphSearchConditionBuilder;
    }

    public GraphSearchConditionBuilderGremlin(String collectionName, Iterable<Vertex> initialVertices)
    {
        this.variableName = collectionName;
        this.initialVertices = initialVertices;
    }

    public static GraphSearchConditionBuilderGremlin create(String collectionName, Iterable<Vertex> initial)
    {
        return new GraphSearchConditionBuilderGremlin(collectionName, initial);
    }

    public void setInitialVertices(Iterable<Vertex> initialVertices)
    {
        this.initialVertices = initialVertices;
    }

    public Iterable<Vertex> getResults(GraphRewrite event)
    {
        Iterable<Vertex> vertices;
        if (initialVertices == null)
        {
            vertices = graphSearchConditionBuilder.getResults(event);
        }
        else
        {
            vertices = initialVertices;
        }
        pipeline.setStarts(vertices);

        return pipeline;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        Set<WindupVertexFrame> frames = new HashSet<>();
        for (Vertex v : getResults(event))
        {
            WindupVertexFrame frame = event.getGraphContext().getFramed().frame(v, WindupVertexFrame.class);
            frames.add(frame);
        }

        VarStack varStack = (VarStack) event.getRewriteContext().get(VarStack.class);
        varStack.setVariable(variableName, frames);

        return !frames.isEmpty();
    }

    public GraphSearchConditionBuilderGremlin addCriterion(GremlinPipelineCriterion criterion)
    {
        criterion.configurePipeline(pipeline);
        return this;
    }
}
