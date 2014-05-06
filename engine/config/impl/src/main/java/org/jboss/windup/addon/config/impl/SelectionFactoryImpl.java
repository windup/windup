/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.windup.addon.config.selectables.Selectable;
import org.jboss.windup.addon.config.selectables.SelectableCondition;
import org.jboss.windup.addon.config.spi.SelectionFactory;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
@Singleton
public class SelectionFactoryImpl implements SelectionFactory
{
    @Inject
    private AddonRegistry registry;

    @Override
    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD> CONDITION createQuery(
                Class<SELECTABLE> selectable, String var)
    {
        SELECTABLE selectableInstance = registry.getServices(selectable).get();
        Class<CONDITION> conditionType = selectableInstance.getSelectableConditionType();
        CONDITION conditionInstance = registry.getServices(conditionType).get();
        conditionInstance.setCollectionName(var);
        return conditionInstance;
    }

    @Override
    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD> Iterable<SELECTABLE> getQueryResult(
                Class<SELECTABLE> type, String var)
    {
        throw new IllegalStateException("Not implemented.");
    }

    @Override
    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD> SELECTABLE get(
                Class<SELECTABLE> selectable, String var)
    {
        SELECTABLE selectableInstance = registry.getServices(selectable).get();
        selectableInstance.setPayload(new LazyCurrentPayload(this, selectable).setVar(var));
        return selectableInstance;
    }

    @Override
    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD> SELECTABLE getCurrent(
                Class<SELECTABLE> selectable)
    {
        SELECTABLE selectableInstance = registry.getServices(selectable).get();
        selectableInstance.setPayload(new LazyCurrentPayload(this, selectable));
        return selectableInstance;
    }

    /*
     * SelectionStack
     */

    Stack<Iterable<?>> stack = new Stack<>();
    HashMap<String, Iterable<?>> vars = new LinkedHashMap<>();
    HashMap<Class<?>, Object> currents = new LinkedHashMap<>();

    @Override
    public void push(Iterable<?> item, String name)
    {
        if (vars.containsKey(name))
            throw new IllegalArgumentException("Variable [" + name
                        + "] already defined. Cannot re-use flow control variables.");

        stack.push(item);
        vars.put(name, item);
    }

    @Override
    public Iterable<?> pop()
    {
        return stack.pop();
    }

    @Override
    public Iterable<?> peek(String name)
    {
        return vars.get(name);
    }

    @Override
    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD> void setCurrentPayload(
                Class<SELECTABLE> type, PAYLOAD element)
    {
        currents.put(type, element);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD> PAYLOAD getCurrentPayload(
                Class<SELECTABLE> type)
    {
        return (PAYLOAD) currents.get(type);
    }

    @Override
    public <SELECTABLE extends Selectable<CONDITION, SELECTABLE, PAYLOAD>, CONDITION extends SelectableCondition<SELECTABLE, CONDITION, PAYLOAD>, PAYLOAD> PAYLOAD getCurrentPayload(
                Class<SELECTABLE> type, String var)
    {
        // TODO implement var selection
        throw new IllegalStateException("Not implemented.");
        // return (PAYLOAD) currents.get(type);
    }
}
