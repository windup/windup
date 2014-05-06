/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.spi;

import org.jboss.windup.addon.config.selectables.Selectable;
import org.jboss.windup.addon.config.selectables.SelectableCondition;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public interface SelectionFactory
{
    void push(Iterable<?> result, String name);

    Iterable<?> pop();

    Iterable<?> peek(String name);

    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD>
                CONDITION createQuery(Class<SELECTABLE> type, String var);

    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD>
                Iterable<SELECTABLE> getQueryResult(Class<SELECTABLE> type, String var);

    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD>
                SELECTABLE get(Class<SELECTABLE> type, String var);

    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD>
                SELECTABLE getCurrent(Class<SELECTABLE> type);

    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD>
                void setCurrentPayload(Class<SELECTABLE> type, PAYLOAD element);

    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD>
                PAYLOAD getCurrentPayload(Class<SELECTABLE> type);

    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD>
                PAYLOAD getCurrentPayload(Class<SELECTABLE> type, String var);
}
