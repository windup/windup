package org.jboss.windup.config.condition;

import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * This is often used with existing value stores as we are simply matching against existing values.
 */
public class NoopEvaluationStrategy implements EvaluationStrategy
{
    @Override
    public void modelMatched()
    {

    }

    @Override
    public void modelSubmissionRejected()
    {
    }

    @Override
    public void modelSubmitted(WindupVertexFrame model)
    {
    }
}
