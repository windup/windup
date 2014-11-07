package org.jboss.windup.config.gremlinquery;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.frames.VertexFromFramedIterable;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Task;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Provides a query system that is based around Gremlin, but with extension to ease it's integration into Windup rules. This will allow multiple
 * conditions to interact in a very fluid and seamless way.
 * 
 * See {@link ComplexJavaConditionTest} for an example of use.
 * 
 * @author jsightler
 */
public class GremlinQuery extends GraphCondition
{
    private final String inputVar;
    private String outputVar = Iteration.DEFAULT_VARIABLE_LIST_STRING;
    private List<GremlinStep> gremlinSteps = new ArrayList<>();

    /**
     * Instantiate a gremlin query over the entire graph.
     */
    public GremlinQuery()
    {
        this.inputVar = null;
    }

    /**
     * Instantiate a gremlin query over an input with the provided name.
     */
    public GremlinQuery(final String inputVar)
    {
        this.inputVar = inputVar;
    }

    /**
     * Instantiate a gremlin query over an input with the provided name.
     */
    public static GremlinQuery from(final String inputVar)
    {
        return new GremlinQuery(inputVar);
    }

    /**
     * Instantiate a gremlin query over an input with the provided type.
     */
    public static GremlinQuery fromType(final Class<? extends WindupVertexFrame> kind)
    {
        GremlinQuery query = new GremlinQuery();
        query.step(new GremlinTypeFilter(kind));
        return query;
    }

    /**
     * Output the results to the specified variable.
     */
    public GremlinQuery as(String outputVar)
    {
        this.outputVar = outputVar;
        return this;
    }

    /**
     * Add the provided {@link GremlinStep} to the pipeline
     */
    public GremlinQuery step(GremlinStep step)
    {
        gremlinSteps.add(step);
        return this;
    }

    @Override
    public boolean evaluate(final GraphRewrite event, final EvaluationContext context)
    {
        List<WindupVertexFrame> results = ExecutionStatistics.performBenchmarked("GremlinQuery(" + this + ")", new Task<List<WindupVertexFrame>>()
        {
            @Override
            public List<WindupVertexFrame> execute()
            {
                final List<WindupVertexFrame> output = new ArrayList<>();
                Iterable<?> values = query(event);
                for (Object value : values)
                {
                    if (!(value instanceof Vertex))
                    {
                        throw new WindupException("Query must return a value of type Vertex, but " + this + " did not!");
                    }
                    Vertex vertex = (Vertex) value;
                    output.add(event.getGraphContext().getFramed().frame(vertex, WindupVertexFrame.class));

                }
                Variables.instance(event).setVariable(outputVar, output);
                return output;
            }
        });
        return !results.isEmpty();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Iterable<?> query(final GraphRewrite event)
    {
        GraphContext graphContext = event.getGraphContext();
        GremlinPipeline<?, ?> pipeline;
        if (this.inputVar == null)
        {
            pipeline = new GremlinPipeline<>(graphContext.getGraph());
            pipeline.V();
        }
        else
        {
            Iterable<? extends WindupVertexFrame> inputFrames = Variables.instance(event).findVariable(this.inputVar);
            Iterable<Vertex> inputVertices = new VertexFromFramedIterable(inputFrames);
            pipeline = new GremlinPipeline<>(inputVertices);
        }

        for (final GremlinStep step : gremlinSteps)
        {
            if (step instanceof GremlinQueryFilter)
            {
                GremlinQueryFilterAdapter<?, ?> adapter = new GremlinQueryFilterAdapter(event, pipeline, (GremlinQueryFilter<?, ?>) step);
                pipeline = adapter.execute();
            }
            else if (step instanceof GremlinProgrammaticFilter)
            {
                pipeline.filter(new GremlinProgrammaticFilterAdapter(event, (GremlinProgrammaticFilter<?>) step));
            }
            else if (step instanceof GremlinTransform)
            {
                pipeline.step(new GremlinTransformAdapter(event, (GremlinTransform<?, ?>) step));
            }
            else
            {
                throw new WindupException("Step must be an instance of " + GremlinProgrammaticFilter.class.getSimpleName() + ", "
                            + GremlinQueryFilter.class.getSimpleName() + " or " + GremlinTransform.class.getSimpleName());
            }
        }

        return pipeline;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("GremlinQuery[from=").append(inputVar);
        if (!StringUtils.isBlank(outputVar))
        {
            sb.append(", to=").append(outputVar);
        }
        sb.append("]");

        for (GremlinStep step : this.gremlinSteps)
        {
            sb.append(".step(");
            sb.append(step);
            sb.append(")");
        }
        return sb.toString();
    }
}
