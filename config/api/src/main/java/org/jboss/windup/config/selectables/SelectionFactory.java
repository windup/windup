/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.selectables;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.exception.IllegalTypeArgumentException;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.exception.WindupException;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class SelectionFactory
{
    /*
     * SelectionStack
     */
    Deque<Map<String, Iterable<WindupVertexFrame>>> deque = new LinkedList<>();
    Map<String, WindupVertexFrame> currents = new HashMap<>();

    public static SelectionFactory instance(GraphRewrite event)
    {
        return (SelectionFactory) event.getRewriteContext().get(SelectionFactory.class);
    }

    public void push()
    {
        Map<String, Iterable<WindupVertexFrame>> newFrame = new HashMap<>();
        deque.push(newFrame);
    }

    public Map<String, Iterable<WindupVertexFrame>> pop()
    {
        return deque.pop();
    }

    private Map<String, Iterable<WindupVertexFrame>> peek()
    {
        return deque.peek();
    }

    public void setVariable(String name, Iterable<WindupVertexFrame> iterable)
    {
        Map<String, Iterable<WindupVertexFrame>> frame = peek();
        if (findVariable(name) != null)
        {
            throw new IllegalArgumentException("Variable \"" + name
                        + "\" has already been assigned and cannot be reassigned");
        }
        frame.put(name, iterable);
    }

    @SuppressWarnings("unchecked")
    public <T extends WindupVertexFrame> T findSingletonVariable(Class<T> type, String name)
    {
        Iterable<WindupVertexFrame> frames = findVariable(name);
        T result = null;
        Iterator<WindupVertexFrame> iterator = frames.iterator();
        if (iterator.hasNext())
        {
            Object foundObject = iterator.next();
            if (!type.isAssignableFrom(foundObject.getClass()))
            {
                throw new IllegalTypeArgumentException(name, type, foundObject.getClass());
            }
            result = (T) foundObject;

            if (iterator.hasNext())
            {
                throw new WindupException("findSingleton called for variable \"" + name
                            + "\", but more than one result is present.");
            }
        }
        return result;
    }

    public Iterable<WindupVertexFrame> findVariable(String name)
    {
        Iterator<Map<String, Iterable<WindupVertexFrame>>> descIter = deque.descendingIterator();
        Iterable<WindupVertexFrame> result = null;
        while (descIter.hasNext())
        {
            Map<String, Iterable<WindupVertexFrame>> frame = descIter.next();
            result = frame.get(name);
            if (result != null)
            {
                break;
            }
        }
        return result;
    }

    public void setCurrentPayload(String name, WindupVertexFrame element)
    {
        currents.put(name, element);
    }

    @SuppressWarnings("unchecked")
    public <T extends WindupVertexFrame> T getCurrentPayload(Class<T> type, String name)
    {
        Object object = currents.get(name);
        if (object == null)
        {
            return null;
        }
        else
        {
            if (!type.isAssignableFrom(object.getClass()))
            {
                throw new IllegalTypeArgumentException(name, type, object.getClass());
            }
            return (T) object;
        }
    }
}
