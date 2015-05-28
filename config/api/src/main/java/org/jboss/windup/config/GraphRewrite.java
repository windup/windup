/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import java.util.Collections;

import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.AbstractRewrite;
import org.ocpsoft.rewrite.event.Flow;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class GraphRewrite extends AbstractRewrite implements Rewrite
{
    private final GraphContext graphContext;
    private final Iterable<RuleLifecycleListener> listeners;

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
    public void ruleEvaluationProgress(String name, int currentPosition, int total, int timeRemainingInSeconds)
    {
        for (RuleLifecycleListener listener : listeners)
            listener.ruleEvaluationProgress(this, name, currentPosition, total, timeRemainingInSeconds);
    }
}
