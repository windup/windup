package org.jboss.windup.config.parameters;

import java.util.Map;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.param.ParameterValueStore;

/**
 * Used during
 * {@link ParameterizedGraphCondition#evaluateAndPopulateValueStores(org.jboss.windup.config.GraphRewrite, org.ocpsoft.rewrite.context.EvaluationContext, FrameCreationContext)}
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface FrameCreationContext
{
    /**
     * Add a new {@link ParameterValueStore} frame for future processing.
     */
    void beginNew(Map<String, Iterable<? extends WindupVertexFrame>> variables);

    /**
     * Remove the previously added {@link ParameterValueStore} frame.
     */
    void rollback();
}