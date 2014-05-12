package org.jboss.windup.addon.config.graphsearch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.condition.GraphCondition;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.model.meta.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class GraphSearchConditionBuilderGremlin extends GraphCondition
{
    private String variableName;
    private GraphSearchConditionBuilder graphSearchConditionBuilder;
    private Iterable<Vertex> initialVertices;

    private List<GraphSearchGremlinCriterion> criteria = new ArrayList<>();
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

    public static GraphSearchConditionBuilderGremlin create()
    {
        return new GraphSearchConditionBuilderGremlin();
    }

    public static GraphSearchConditionBuilderGremlin create(String collectionName, Iterable<Vertex> initial)
    {
        return new GraphSearchConditionBuilderGremlin(collectionName, initial);
    }

    public GraphSearchConditionBuilderGremlin withCriterion(GraphSearchGremlinCriterion criterion)
    {
        criteria.add(criterion);
        return this;
    }

    public void setInitialVertices(Iterable<Vertex> initialVertices)
    {
        this.initialVertices = initialVertices;
    }

    public Iterable<Vertex> getResults(GraphRewrite event)
    {
        for (GraphSearchGremlinCriterion c : criteria)
        {
            c.query(pipeline);
        }

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

        SelectionFactory factory = (SelectionFactory) event.getRewriteContext().get(SelectionFactory.class);
        factory.push(frames, variableName);

        return !frames.isEmpty();
    }
}
