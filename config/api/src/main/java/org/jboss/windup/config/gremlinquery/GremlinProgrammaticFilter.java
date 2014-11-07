package org.jboss.windup.config.gremlinquery;

import org.jboss.windup.config.GraphRewrite;

import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * A Java-based filter that restricts which items flow through the {@link GremlinPipeline}.
 */
public interface GremlinProgrammaticFilter<INPUT_TYPE> extends GremlinStep
{
    /**
     * Returns true if the provided item should be passed through, false otherwise.
     */
    public boolean filter(GraphRewrite event, INPUT_TYPE value);
}
