/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.runner;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Vetoed;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.exception.IllegalTypeArgumentException;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * A variables stack - keeps few layers of "key"->[vertices] maps, one per rule execution level.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Vetoed
public class VarStack
{
    private final Deque<Map<String, Iterable<WindupVertexFrame>>> deque = new LinkedList<>();

    private VarStack()
    {
    }

    /**
     * Gets an instance from a OCP rewrite context.
     */
    public static VarStack instance(GraphRewrite event)
    {
        VarStack instance = (VarStack) event.getRewriteContext().get(VarStack.class);
        if (instance == null)
        {
            instance = new VarStack();
            event.getRewriteContext().put(VarStack.class, instance);
        }
        return instance;
    }

    /**
     * Add new variables layer on top of the stack.
     */
    public void push()
    {
        Map<String, Iterable<WindupVertexFrame>> newFrame = new HashMap<>();
        deque.push(newFrame);
    }

    /**
     * Remove the top variables layer from the the stack.
     */
    public Map<String, Iterable<WindupVertexFrame>> pop()
    {
        return deque.pop();
    }

    /**
     * Get the top variables layer from the stack.
     */
    public Map<String, Iterable<WindupVertexFrame>> peek()
    {
        return deque.peek();
    }

    /**
     * Type-safe wrapper around setVariable which sets only one framed vertex.
     */
    public void setSingletonVariable(String string, WindupVertexFrame frame)
    {
        List<WindupVertexFrame> singleton = new ArrayList<>();
        singleton.add(frame);
        setVariable(string, singleton);
    }

    /**
     * Set a variable in the top variables layer to given "collection" of the vertex frames. Can't be reassigned -
     * throws on attempt to reassign.
     */
    public void setVariable(String name, Iterable<WindupVertexFrame> frames)
    {
        Map<String, Iterable<WindupVertexFrame>> frame = peek();
        if (findVariable(name) != null)
        {
            throw new IllegalArgumentException("Variable \"" + name
                        + "\" has already been assigned and cannot be reassigned");
        }

        frame.put(name, frames);
    }

    /**
     * Type-safe wrapper around findVariable which gives only one framed vertex, and checks if there is 0 or 1; throws
     * otherwise.
     */
    @SuppressWarnings("unchecked")
    public <T extends WindupVertexFrame> T findSingletonVariable(Class<T> type, String name)
    {
        Iterable<WindupVertexFrame> frames = findVariable(name);
        if (null == frames)
        {
            throw new IllegalStateException("Variable not found: " + name);
        }

        Iterator<WindupVertexFrame> iterator = frames.iterator();
        if (!iterator.hasNext())
        {
            return null;
        }

        Object obj = iterator.next();

        if (iterator.hasNext())
        {
            throw new IllegalStateException("More than one frame present "
                        + "under presumed singleton variable: " + name);
        }

        if (type != null && !type.isAssignableFrom(obj.getClass()))
        {
            throw new IllegalTypeArgumentException(name, type, obj.getClass());
        }

        return (T) obj;
    }

    /**
     * Searches the variables layers, top to bottom, for given name, and returns if found; null otherwise.
     */
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
}
