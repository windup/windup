package org.jboss.windup.config.gremlinquery;

import org.jboss.windup.config.GraphRewrite;

import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * This allows a step to manipulate the pipeline directly. This could be useful in some cases to improve the performance vs programmatic filtering.
 */
public interface GremlinQueryFilter<INPUT_TYPE, OUTPUT_TYPE> extends GremlinStep
{
    /**
     * Takes the provided {@link GremlinePipeline} and tweaks (or replaces) it, returning the new result.
     */
    public GremlinPipeline<INPUT_TYPE, OUTPUT_TYPE> process(GraphRewrite event, GremlinPipeline<INPUT_TYPE, OUTPUT_TYPE> pipe);
}
