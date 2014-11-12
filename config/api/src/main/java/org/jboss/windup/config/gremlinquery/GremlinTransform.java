package org.jboss.windup.config.gremlinquery;

import org.jboss.windup.config.GraphRewrite;

/**
 * Transforms the provided input into a different result. If the result is an Iterable, this will get automatically flattened (similar to gremlin's
 * builtin scatter operation) by the {@link GremlinQuery} system.
 */
public interface GremlinTransform<INPUT_TYPE, OUTPUT_TYPE> extends GremlinStep
{
    /**
     * Transforms the provided input. If the result is an Iterable, this will get automatically flattened (similar to gremlin's builtin scatter
     * operation) by the {@link GremlinQuery} system.
     */
    public OUTPUT_TYPE transform(GraphRewrite event, INPUT_TYPE input);
}
