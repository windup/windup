package org.jboss.windup.config.condition;

import org.jboss.windup.config.parameters.ParameterizedGraphCondition;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.param.ParameterValueStore;

/**
 * This is used by {@link ParameterizedGraphCondition} during parameter evaluation.
 *
 */
public interface EvaluationStrategy
{
    /**
     * Indicates that we have potentially found a match.
     */
    void modelMatched();

    /**
     * Adds the model to the list matched by this set of parameter values.
     */
    void modelSubmitted(WindupVertexFrame model);

    /**
     * Indicates that the match has been rejected, and we should reject any variables stored in the {@link ParameterValueStore} from this match.
     */
    void modelSubmissionRejected();
}
