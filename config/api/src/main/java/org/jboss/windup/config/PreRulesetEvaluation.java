package org.jboss.windup.config;

import org.ocpsoft.rewrite.config.Rule;

/**
 * Denotes an element that takes some action before the execution of the {@link Rule} pipeline.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface PreRulesetEvaluation
{
    /**
     * The action to be performed before evaluation of the compiled {@link Rule} set.
     */
    public void preRulesetEvaluation(GraphRewrite event);
}
