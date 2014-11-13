package org.jboss.windup.config.gremlinquery;

import org.jboss.windup.config.GraphRewrite;

import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.IdentityPipe;
import com.tinkerpop.pipes.Pipe;

/**
 * Converts the Windup-style {@link GremlinQueryFilter} into something that can be used within a {@link GremlinPipeline}.
 */
class GremlinQueryFilterAdapter<PIPE_IN, PIPE_OUT>
{
    private final GraphRewrite event;
    private final GremlinPipeline<PIPE_IN, PIPE_OUT> pipeline;
    private final GremlinQueryFilter<PIPE_IN, PIPE_OUT> filter;

    public GremlinQueryFilterAdapter(GraphRewrite event, GremlinPipeline<PIPE_IN, PIPE_OUT> pipeline, GremlinQueryFilter<PIPE_IN, PIPE_OUT> filter)
    {
        this.event = event;
        this.pipeline = pipeline;
        this.filter = filter;
    }

    protected GremlinPipeline<PIPE_IN, PIPE_OUT> execute()
    {
        GremlinPipeline<PIPE_IN, PIPE_OUT> result = filter.process(event, this.pipeline);
        result.step((Pipe<PIPE_OUT, PIPE_OUT>) new IdentityPipe<PIPE_OUT>()
        {
            @SuppressWarnings("unchecked")
            @Override
            protected PIPE_OUT processNextStart()
            {
                Object result = super.processNextStart();
                return (PIPE_OUT) result;
            }
        });
        return result;
    }
}
