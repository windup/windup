/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.selectables;

import org.jboss.windup.addon.config.spi.SelectionFactory;
import org.ocpsoft.common.services.ServiceLoader;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Selection
{
    /*
     * Conditions
     */
    public static <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD>
                CONDITION ofAll(Class<SELECTABLE> selectable, String var)
    {
        return getSelectionFactory().createQuery(selectable, var);
    }

    public static <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD>
                CONDITION ofCurrent(Class<SELECTABLE> selectable)
    {
        return getSelectionFactory().createQuery(selectable, null);
    }

    /*
     * Selections
     */
    public static <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD>
                SELECTABLE current(Class<SELECTABLE> selectable)
    {
        return getSelectionFactory().getCurrent(selectable);
    }

    public static <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD>
                SELECTABLE get(Class<SELECTABLE> selectable, String var)
    {
        return getSelectionFactory().get(selectable, var);
    }

    /*
     * Private
     */
    private static SelectionFactory getSelectionFactory()
    {
        return (SelectionFactory) ServiceLoader.load(SelectionFactory.class).iterator().next();
    }
}
