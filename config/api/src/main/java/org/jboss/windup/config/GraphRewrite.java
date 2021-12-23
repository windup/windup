/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import java.util.Collections;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.exception.WindupStopException;
import org.ocpsoft.rewrite.AbstractRewrite;
import org.ocpsoft.rewrite.event.Flow;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * Holds the context of the whole Windup Execution.
 * TODO: should be called "WindupExecutionContext" then?
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class GraphRewrite extends AbstractRewrite implements Rewrite
{
    private final GraphContext graphContext;
    private final Iterable<RuleLifecycleListener> listeners;
    private WindupStopException windupStopException;

    public GraphRewrite(GraphContext context)
    {
        this.listeners = Collections.emptyList();
        this.graphContext = context;
    }

    public GraphRewrite(Iterable<RuleLifecycleListener> listeners, GraphContext context)
    {
        this.listeners = listeners;
        this.graphContext = context;
    }

    /**
     * @return The exception which holds information where Windup has stopped before finishing (typically on an external request),
     *      or null if Windup finished normally.
     */
    public WindupStopException getWindupStopException()
    {
        return windupStopException;
    }

    /**
     * Stores the exception which holds information if, and where, the Windup stopped (typically on an external request).
     * If windup was stopped, this must be called.
     */
    public void setWindupStopException(WindupStopException windupStopException)
    {
        if (this.windupStopException != null)
            throw new WindupException("Trying to set the stop exception while it was already set."
                    + " The cause contains the original one.", this.windupStopException);
        this.windupStopException = windupStopException;
    }



    // TODO: This deserves a javadoc.
    @Override
    public Flow getFlow()
    {
        return new Flow()
        {
            @Override
            public boolean isHandled()
            {
                return false;
            }

            @Override
            public boolean is(Flow type)
            {
                return false;
            }
        };
    }

    public GraphContext getGraphContext()
    {
        return graphContext;
    }

    /**
     * This is optionally called by long-running rules to indicate their current progress and estimated time-remaining.
     */
    public boolean ruleEvaluationProgress(String name, int currentPosition, int total, int timeRemainingInSeconds)
    {
        boolean windupStopRequested = false;
        for (RuleLifecycleListener listener : listeners)
            windupStopRequested = listener.ruleEvaluationProgress(this, name, currentPosition, total, timeRemainingInSeconds);

        return windupStopRequested;
    }

    public boolean shouldWindupStop()
    {
        for (RuleLifecycleListener listener : listeners)
            if (listener.shouldWindupStop())
                return true;
        return false;
    }
}
