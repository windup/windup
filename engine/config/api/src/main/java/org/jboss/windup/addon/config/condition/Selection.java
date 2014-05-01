/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.condition;

import org.jboss.windup.addon.config.selectables.Selectable;
import org.jboss.windup.addon.config.selectables.SelectableCondition;
import org.jboss.windup.addon.config.spi.SelectionFactory;
import org.ocpsoft.common.services.ServiceLoader;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Selection
{
    public static <SELECTABLE extends Selectable<CONDITION, SELECTABLE>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION>> CONDITION exists(Class<SELECTABLE> selectable, String var)
    {
        return getSelectionFactory().createQuery(selectable);
    }

    public static <SELECTABLE extends Selectable<CONDITION, SELECTABLE>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION>> SELECTABLE current(
                Class<SELECTABLE> selectable)
    {
        return getSelectionFactory().getCurrent(selectable);
    }

    public static <SELECTABLE extends Selectable<CONDITION, SELECTABLE>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION>> SELECTABLE get(
                Class<SELECTABLE> selectable, String var)
    {
        return getSelectionFactory().get(selectable);
    }

    private static SelectionFactory getSelectionFactory()
    {
        return (SelectionFactory) ServiceLoader.load(SelectionFactory.class).iterator().next();
    }
}
