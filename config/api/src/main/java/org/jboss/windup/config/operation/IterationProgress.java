package org.jboss.windup.config.operation;

import java.util.logging.Logger;

import org.jboss.forge.furnace.util.Iterators;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Provides a simplistic way of printing a message to the log every {@link IterationProgress#interval} iterations.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class IterationProgress extends AbstractIterationOperation<WindupVertexFrame>
{
    private static final Logger LOG = Logging.get(IterationProgress.class);

    private String messagePrefix;
    private int interval;
    private int totalIterations = -1;
    private int currentIteration = 0;

    public IterationProgress(String messagePrefix, int interval)
    {
        this.messagePrefix = messagePrefix;
        this.interval = interval;
    }

    public static IterationProgress monitoring(String messagePrefix, int interval)
    {
        return new IterationProgress(messagePrefix, interval);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload)
    {
        if (totalIterations == -1)
        {
            @SuppressWarnings("unchecked")
            Iterable<WindupVertexFrame> frames = (Iterable<WindupVertexFrame>) event.getRewriteContext().get(
                        Iteration.DEFAULT_VARIABLE_LIST_STRING);
            totalIterations = Iterators.asList(frames).size();
        }
        currentIteration++;
        if (currentIteration % interval == 0)
        {
            LOG.info(messagePrefix + currentIteration + " / " + totalIterations);
        }
    }
}
