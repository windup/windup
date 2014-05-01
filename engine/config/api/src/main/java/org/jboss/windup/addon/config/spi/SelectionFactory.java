/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.spi;

import org.jboss.windup.addon.config.Selectable;
import org.jboss.windup.addon.config.SelectableCondition;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public interface SelectionFactory
{
    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION>> CONDITION createQuery(
                Class<SELECTABLE> selectable);

    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION>> SELECTABLE get(
                Class<SELECTABLE> selectable);

    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION>> SELECTABLE getCurrent(
                Class<SELECTABLE> selectable);
}
