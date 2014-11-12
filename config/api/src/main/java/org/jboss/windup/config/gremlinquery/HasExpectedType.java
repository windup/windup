package org.jboss.windup.config.gremlinquery;

import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * A {@link GremlinStep} that implements this interface is providing a hint to the system of what types of items should be queries.
 * 
 * Note that this is only a hint, and will only be used in certain circumstances (generally when there is no other information already available on
 * what types to input).
 */
public interface HasExpectedType
{
    /**
     * Returns the Type hint for use in query optimization
     */
    public Class<? extends WindupVertexFrame> getExpectedTypeHint();
}
