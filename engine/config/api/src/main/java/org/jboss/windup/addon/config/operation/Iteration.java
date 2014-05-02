/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.operation;

import org.jboss.windup.addon.config.selectables.Selectable;
import org.jboss.windup.addon.config.selectables.SelectableCondition;
import org.jboss.windup.addon.config.spi.SelectionFactory;
import org.ocpsoft.common.services.ServiceLoader;
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

    private final Class<? extends Selectable<?, ?>> type;
    private final String source;
    private final String var;
    private Class<?> castType;

    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION>> Iteration(
                Class<SELECTABLE> type, String source, String var)
    {
        this.type = type;
        this.source = source;
        this.var = var;
    }

    /**
     * Begin an {@link Iteration}
     */
    public static <SELECTABLE extends Selectable<CONDITION, SELECTABLE>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION>> Iteration over(
                Class<SELECTABLE> selectable, String source, String var)
    {
        return new Iteration(selectable, source, var);
    }

    /**
     * Cast each iterated element to the given type (if possible.)
     */
    public Iteration as(Class<?> castType)
    {
        this.castType = castType;
        return this;
    }

    public Iteration when(Condition condition)
    {
        return this;
    }

    public Iteration perform(Operation operation)
    {
        return this;
    }

    /*
     * Ideally this method is encapsulated in an implementation class that the user would never see when configuring an
     * Iteration
     */
    @Override
    public void perform(Rewrite event, EvaluationContext context)
    {

    }

    private static SelectionFactory getSelectionFactory()
    {
        return (SelectionFactory) ServiceLoader.load(SelectionFactory.class).iterator().next();
    }
}
