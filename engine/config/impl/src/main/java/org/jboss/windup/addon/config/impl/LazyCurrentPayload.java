/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.impl;

import java.util.concurrent.Callable;

import org.jboss.windup.addon.config.selectables.Selectable;
import org.jboss.windup.addon.config.selectables.SelectableCondition;
import org.jboss.windup.addon.config.spi.SelectionFactory;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class LazyCurrentPayload<SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD>
            implements Callable<PAYLOAD>
{
    private final SelectionFactory factory;
    private final Class<SELECTABLE> selectableType;
    private String var;

    public LazyCurrentPayload(SelectionFactory factory, Class<SELECTABLE> selectableType)
    {
        this.factory = factory;
        this.selectableType = selectableType;
    }

    @Override
    public PAYLOAD call() throws Exception
    {
        PAYLOAD result;
        if (var != null)
        {
            result = factory.getCurrentPayload(selectableType, var);
        }
        else
        {
            result = factory.getCurrentPayload(selectableType);
        }
        return result;
    }

    public LazyCurrentPayload<SELECTABLE, CONDITION, PAYLOAD> setVar(String var)
    {
        this.var = var;
        return this;
    }
}
