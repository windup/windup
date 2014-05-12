/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.selectables;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.graph.model.meta.WindupVertexFrame;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class SelectionFactory
{
    /*
     * SelectionStack
     */

    Stack<Iterable<WindupVertexFrame>> stack = new Stack<>();
    HashMap<String, Iterable<WindupVertexFrame>> vars = new LinkedHashMap<>();
    HashMap<Class<? extends WindupVertexFrame>, WindupVertexFrame> currents = new LinkedHashMap<>();

    public void push(Iterable<WindupVertexFrame> item, String name)
    {
        if (vars.containsKey(name))
            throw new IllegalArgumentException("Variable [" + name
                        + "] already defined. Cannot re-use flow control variables.");

        stack.push(item);
        vars.put(name, item);
    }

    public static SelectionFactory instance(GraphRewrite event)
    {
        return (SelectionFactory) event.getRewriteContext().get(SelectionFactory.class);
    }

    public Iterable<WindupVertexFrame> pop()
    {
        return stack.pop();
    }

    public Iterable<WindupVertexFrame> peek(String name)
    {
        return vars.get(name);
    }

    public void setCurrentPayload(Class<? extends WindupVertexFrame> type, WindupVertexFrame element)
    {
        currents.put(type, element);
    }

    @SuppressWarnings("unchecked")
    public <T extends WindupVertexFrame> T getCurrentPayload(Class<T> type)
    {
        return (T) currents.get(type);
    }
}
