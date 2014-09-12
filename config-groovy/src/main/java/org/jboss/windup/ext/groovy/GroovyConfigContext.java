package org.jboss.windup.ext.groovy;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.graph.GraphContext;

/**
 * Context for creating {@link GroovyConfigMethod} closure callbacks.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 *
 */
public interface GroovyConfigContext
{
    /**
     * Add a {@link WindupRuleProvider}.
     */
    void addRuleProvider(WindupRuleProvider provider);

    /**
     * Get the {@link GraphContext}
     */
    GraphContext getGraphContext();
}
