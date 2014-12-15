package org.jboss.windup.config.parameters;

/**
 * Used during
 * {@link ParameterizedGraphCondition#evaluateWithValueStore(org.jboss.windup.config.GraphRewrite, org.ocpsoft.rewrite.context.EvaluationContext, FrameContext)}
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface FrameContext
{
    /**
     * Reject the current frame and remove it from future processing.
     */
    void reject();
}