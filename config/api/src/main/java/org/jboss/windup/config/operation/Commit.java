package org.jboss.windup.config.operation;

import org.jboss.windup.config.GraphRewrite;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class Commit extends GraphOperation
{
    private int period;
    private int uncommittedIterations = 0;

    private Commit(int period)
    {
        this.period = period;
    }

    public static Commit every(int period)
    {
        return new Commit(period);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        uncommittedIterations++;
        if (uncommittedIterations > period)
        {
            event.getGraphContext().getGraph().getBaseGraph().commit();
            uncommittedIterations = 0;
        }
    }

    @Override
    public String toString()
    {
        return "Commit.every(" + period + ")";
    }
}
