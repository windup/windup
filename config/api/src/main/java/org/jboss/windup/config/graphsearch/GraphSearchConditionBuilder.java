package org.jboss.windup.config.graphsearch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.runner.VarStack;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;

/**
 * The GraphSearchConditionBuilderGremlin criteria just exposes the GremlinPipeline
 */
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
    
    public GraphSearchConditionBuilder addSearchCriterion(GraphSearchCriterion criterion) 
    {
    	graphSearchCriteria.add(criterion);
    	return this;
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

    public GraphSearchConditionBuilder withProperty(String property, Object searchValue)
    {
        return withProperty(property, GraphSearchPropertyComparisonType.EQUALS, searchValue);
    }

    public GraphSearchConditionBuilder withProperty(String property, GraphSearchPropertyComparisonType searchType,
                Object searchValue)
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

        // Frame the vertexes.
        Set<WindupVertexFrame> frames = new HashSet<>();
        for (Vertex v : vertices)
        {
            WindupVertexFrame frame = event.getGraphContext().getFramed().frame(v, WindupVertexFrame.class);
            frames.add(frame);
        }

        // Put the variable to the stack.
        VarStack varStack = (VarStack) event.getRewriteContext().get(VarStack.class);
        varStack.setVariable(variableName, frames);

        return !frames.isEmpty();
    }

}
