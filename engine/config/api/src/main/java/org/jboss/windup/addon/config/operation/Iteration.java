/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.operation;

import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.model.meta.WindupVertexFrame;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.DefaultOperationBuilder;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class Iteration extends DefaultOperationBuilder
{

    private final Class<? extends WindupVertexFrame> type;
    private final String source;
    private final String var;
    private Condition condition;
    private Operation operation;

    public Iteration(
                Class<? extends WindupVertexFrame> type, String source, String var)
    {
        this.type = type;
        this.source = source;
        this.var = var;
    }

    /**
     * Begin an {@link Iteration}
     */
    public static Iteration over(Class<? extends WindupVertexFrame> selectable, String source, String var)
    {
        return new Iteration(selectable, source, var);
    }

    public Iteration when(Condition condition)
    {
        this.condition = condition;
        return this;
    }

    public Iteration perform(Operation operation)
    {
        this.operation = operation;
        return this;
    }

    /*
     * Ideally this method is encapsulated in an implementation class that the user would never see when configuring an
     * Iteration
     */
    @Override
    @SuppressWarnings("unchecked")
    public void perform(Rewrite event, EvaluationContext context)
    {
        if (operation != null)
        {
            SelectionFactory factory = (SelectionFactory) event.getRewriteContext().get(SelectionFactory.class);
            Iterable<WindupVertexFrame> peek = factory.peek(source);
            for (WindupVertexFrame element : peek)
            {
                factory.setCurrentPayload(type, element);
                if (condition == null || condition.evaluate(event, context))
                {
                    if (operation != null)
                        operation.perform(event, context);
                }
            }
            factory.setCurrentPayload((Class) type, null);
        }
    }
}
