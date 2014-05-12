package org.jboss.windup.addon.config.operation;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.graphsearch.GraphSearchConditionBuilderGremlin;
import org.jboss.windup.addon.config.graphsearch.GraphSearchGremlinCriterion;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.model.meta.WindupVertexFrame;

import com.tinkerpop.blueprints.Vertex;

public class IterationQuery extends Iteration
{

    private final GraphSearchConditionBuilderGremlin graphSearchConditionBuilderGremlin;

    public IterationQuery(
                Class<? extends WindupVertexFrame> type,
                String source, String var)
    {
        super(type, source, var);
        this.graphSearchConditionBuilderGremlin = new GraphSearchConditionBuilderGremlin();
    }

    public IterationQuery withCriterion(GraphSearchGremlinCriterion criterion)
    {
        graphSearchConditionBuilderGremlin.withCriterion(criterion);
        return this;
    }

    Iterable<WindupVertexFrame> findFrames(GraphRewrite event, SelectionFactory factory)
    {
        if (graphSearchConditionBuilderGremlin != null)
        {
            Iterable<WindupVertexFrame> initialFrames = factory.peek(getSource());
            List<Vertex> initialVertices = new ArrayList<>();
            for (WindupVertexFrame frame : initialFrames)
            {
                initialVertices.add(frame.asVertex());
            }

            graphSearchConditionBuilderGremlin.setInitialVertices(initialVertices);
            Iterable<Vertex> v = graphSearchConditionBuilderGremlin.getResults(event);
            return GraphUtil.toVertexFrames(event.getGraphContext(), v);
        }
        else
        {
            return factory.peek(getSource());
        }
    }
}
