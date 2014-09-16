package org.jboss.windup.config.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.selectors.FramesSelector;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class Query extends GraphCondition implements QueryBuilderFind, QueryBuilderFrom, QueryBuilderWith,
            QueryBuilderPiped
{
    private String outputVar = Iteration.DEFAULT_VARIABLE_LIST_STRING;

    private final List<QueryFramesCriterion> criteria = new ArrayList<>();
    private final List<QueryGremlinCriterion> pipelineCriteria = new ArrayList<>();

    private FramesSelector framesSelector;

    private Predicate<WindupVertexFrame> resultFilter;

    private Query()
    {
    }

    /**
     * Begin this {@link Query} with all Frame instances that are the result of the provided GremlinQueryCriterion.
     */
    public static QueryBuilderPiped gremlin(final QueryGremlinCriterion criterion)
    {
        final Query query = new Query();
        query.setInitialFramesSelector(new FramesSelector()
        {
            @Override
            public Iterable<WindupVertexFrame> getFrames(GraphRewrite event, EvaluationContext context)
            {
                GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<Vertex, Vertex>(event.getGraphContext()
                            .getGraph());
                criterion.query(event, pipeline);
                return new FramedVertexIterable<WindupVertexFrame>(event.getGraphContext().getFramed(), pipeline,
                            WindupVertexFrame.class);
            }
        });
        return query;
    }

    /**
     * Begin this {@link Query} with all {@link WindupVertexFrame} instances of the given type.
     */
    public static QueryBuilderFind find(Class<? extends WindupVertexFrame> type)
    {
        final Query query = new Query();
        query.setInitialFramesSelector(new FramesSelector()
        {
            @Override
            public Iterable<WindupVertexFrame> getFrames(GraphRewrite event, EvaluationContext context)
            {
                FramedGraphQuery graphQuery = event.getGraphContext().getFramed().query();
                for (QueryFramesCriterion c : query.getCriteria())
                {
                    c.query(graphQuery);
                }

                Set<WindupVertexFrame> frames = new HashSet<>();
                for (Vertex v : graphQuery.vertices())
                {
                    WindupVertexFrame frame = event.getGraphContext().getFramed().frame(v, WindupVertexFrame.class);
                    frames.add(frame);
                }
                return frames;
            }
        });
        query.with(new QueryTypeCriterion(type));
        return query;
    }

    /**
     * Begin this {@link Query} with results of a prior {@link Query}, read from the variable with the given name.
     */
    public static QueryBuilderFrom from(final String name)
    {
        Query query = new Query();
        query.setInitialFramesSelector(new FramesSelector()
        {
            @Override
            public Iterable<WindupVertexFrame> getFrames(GraphRewrite event, EvaluationContext context)
            {
                Variables variables = (Variables) event.getRewriteContext().get(Variables.class);
                return variables.findVariable(name);
            }
        });

        return query;
    }

    @Override
    public ConditionBuilder as(String name)
    {
        outputVar = name;
        return this;
    }

    /*
     * Evaluators
     */

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        Iterable<WindupVertexFrame> frames = framesSelector.getFrames(event, context);

        List<Vertex> vertices = new ArrayList<>();
        for (WindupVertexFrame frame : frames)
        {
            vertices.add(frame.asVertex());
        }

        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(vertices);
        if (!pipelineCriteria.isEmpty() && frames != null && frames.iterator().hasNext())
        {
            for (QueryGremlinCriterion criterion : pipelineCriteria)
            {
                criterion.query(event, pipeline);
            }
        }

        List<WindupVertexFrame> result = new ArrayList<>();
        for (Vertex vertex : pipeline)
        {
            result.add(event.getGraphContext().getFramed().frame(vertex, WindupVertexFrame.class));
        }

        if (resultFilter != null)
        {
            List<WindupVertexFrame> filtered = new LinkedList<>();
            for (WindupVertexFrame frame : result)
            {
                if (resultFilter.accept(frame))
                {
                    filtered.add(frame);
                }
            }
            result.retainAll(filtered);
        }

        Variables variables = (Variables) event.getRewriteContext().get(Variables.class);
        variables.setVariable(outputVar, result);

        return result.iterator().hasNext();
    }

    /*
     * Criteria
     */

    @Override
    public QueryBuilderWith withProperty(String property, Object searchValue)
    {
        return withProperty(property, QueryPropertyComparisonType.EQUALS, searchValue);
    }

    @Override
    public QueryBuilderWith withProperty(String property, Iterable<?> values)
    {
        criteria.add(new QueryPropertyCriterion(property, QueryPropertyComparisonType.CONTAINS_ANY_TOKEN, values));
        return this;
    }

    @Override
    public QueryBuilderWith withProperty(String property, Object searchValue, Object... searchValues)
    {
        List<Object> values = new LinkedList<>();
        values.add(searchValue);
        values.addAll(Arrays.asList(searchValues));

        return withProperty(property, values);
    }

    @Override
    public QueryBuilderWith withProperty(String property, QueryPropertyComparisonType searchType,
                Object searchValue)
    {
        criteria.add(new QueryPropertyCriterion(property, searchType, searchValue));
        return this;
    }

    @Override
    public QueryBuilderWith with(QueryFramesCriterion criterion)
    {
        criteria.add(criterion);
        return this;
    }

    @Override
    public QueryBuilderPiped piped(QueryGremlinCriterion criterion)
    {
        pipelineCriteria.add(criterion);
        return this;
    }

    /*
     * Getters and setters.
     */
    protected List<QueryFramesCriterion> getCriteria()
    {
        return criteria;
    }

    private void setInitialFramesSelector(FramesSelector selector)
    {
        this.framesSelector = selector;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <FRAMETYPE extends WindupVertexFrame> QueryBuilderAs filteredBy(Predicate<FRAMETYPE> predicate)
    {
        this.resultFilter = (Predicate<WindupVertexFrame>) predicate;
        return this;
    }
}
