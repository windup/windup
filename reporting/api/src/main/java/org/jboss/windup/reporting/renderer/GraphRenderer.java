package org.jboss.windup.reporting.renderer;

import org.jboss.windup.graph.GraphContext;

/**
 * Responsible for rendering the given {@link GraphContext}.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface GraphRenderer

{
    /**
     * Render the given {@link GraphContext}.
     */
    public void renderGraph(GraphContext context);
}
