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
import com.tinkerpop.frames.FramedGraphQuery;

public class GraphSearchConditionBuilder extends GraphCondition
{
    private final String variableName;
    private final List<GraphSearchCriterion> graphSearchCriteria = new ArrayList<>();

    private GraphSearchConditionBuilder(String variableName)
    {
        this.variableName = variableName;
    }

    public static GraphSearchConditionBuilder create(String collectionName)
    {
        return new GraphSearchConditionBuilder(collectionName);
    }

    public GraphSearchConditionBuilderGremlin gremlin()
    {
        if (graphSearchCriteria.isEmpty())
        {
            throw new IllegalArgumentException(
                        "You must apply at least one basic filter (type or property based) before switching to Gremlin");
        }
        return new GraphSearchConditionBuilderGremlin(this);
    }

    public GraphSearchConditionBuilder ofType(Class<? extends WindupVertexFrame> clazz)
    {
        graphSearchCriteria.add(new GraphSearchCriterionType(clazz));
        return this;
    }

    public GraphSearchConditionBuilder withProperty(String property, String searchValue)
    {
        return withProperty(property, GraphSearchPropertyComparisonType.EQUALS, searchValue);
    }

    public GraphSearchConditionBuilder withProperty(String property, GraphSearchPropertyComparisonType searchType,
                String searchValue)
    {
        graphSearchCriteria.add(new GraphSearchCriterionProperty(property, searchType, searchValue));
        return this;
    }

    Iterable<Vertex> getResults(GraphRewrite event)
    {
        FramedGraphQuery query = event.getGraphContext().getFramed().query();

        for (GraphSearchCriterion c : graphSearchCriteria)
        {
            c.query(query);
        }

        return query.vertices();
    }

    String getVariableName()
    {
        return variableName;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        Iterable<Vertex> vertices = getResults(event);

        Set<WindupVertexFrame> frames = new HashSet<>();
        for (Vertex v : vertices)
        {
            WindupVertexFrame frame = event.getGraphContext().getFramed().frame(v, WindupVertexFrame.class);
            frames.add(frame);
        }

        SelectionFactory factory = (SelectionFactory) event.getRewriteContext().get(SelectionFactory.class);
        factory.push(frames, variableName);

        return !frames.isEmpty();
    }

}
