/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.selectables;

import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public abstract class AbstractSelectableCondition<SELECTABLE extends Selectable<CONDITION, SELECTABLE>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION>>
            implements SelectableCondition<SELECTABLE, CONDITION>
{
    @Override
    public boolean evaluate(Rewrite event, EvaluationContext context)
    {
        return false;
    }
}
