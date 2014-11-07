package org.jboss.windup.config.gremlinquery;

import org.jboss.windup.config.GraphRewrite;

import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

/**
 * Converts the Windup-style {@link GremlinProgrammaticFilter} into something that can be used within a {@link GremlinPipeline}.
 */
public class GremlinProgrammaticFilterAdapter<INPUT_TYPE> implements PipeFunction<INPUT_TYPE, Boolean>
{
    private final GraphRewrite event;
    private final GremlinProgrammaticFilter<INPUT_TYPE> filter;

    public GremlinProgrammaticFilterAdapter(GraphRewrite event, GremlinProgrammaticFilter<INPUT_TYPE> filter)
    {
        this.event = event;
        this.filter = filter;
    }

    @Override
    public Boolean compute(INPUT_TYPE argument)
    {
        return filter.filter(event, argument);
    }
}
