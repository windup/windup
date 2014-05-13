/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.operation.iteration;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.model.meta.WindupVertexFrame;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class TypedIterationSelectionManager implements IterationSelectionManager
{
    private final Class<? extends WindupVertexFrame> sourceType;

    public TypedIterationSelectionManager(Class<? extends WindupVertexFrame> sourceType)
    {
        this.sourceType = sourceType;
    }

    @Override
    public Iterable<WindupVertexFrame> getFrames(GraphRewrite event, SelectionFactory factory)
    {
        throw new IllegalStateException("Not implemented for typed selections: " + sourceType.getName());
    }

}
