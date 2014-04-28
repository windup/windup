/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.operation;

import org.jboss.windup.addon.config.Selectable;
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

    public static <T extends Selectable> Iteration over(Class<T> selectable, String source, String var)
    {
        return null;
    }

    public Iteration when(Condition condition)
    {
        return null;
    }

    public Iteration perform(Operation operation)
    {
        return null;
    }

    /*
     * Ideally this method is encapsulated in an implementation class that the user would never see when configuring an
     * Iteration
     */
    @Override
    public void perform(Rewrite event, EvaluationContext context)
    {

    }

}
