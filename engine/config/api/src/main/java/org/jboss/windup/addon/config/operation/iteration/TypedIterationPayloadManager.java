/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.operation.iteration;

import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.model.meta.WindupVertexFrame;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class TypedIterationPayloadManager implements IterationPayloadManager
{
    private final Class<? extends WindupVertexFrame> sourceType;
    private WindupVertexFrame previousPayload;

    public TypedIterationPayloadManager(Class<? extends WindupVertexFrame> sourceType)
    {
        this.sourceType = sourceType;
    }

    @Override
    public void setCurrentPayload(SelectionFactory factory, WindupVertexFrame element)
    {
        String name = sourceType.getName();
        this.previousPayload = factory.getCurrentPayload(sourceType, name);
        factory.setCurrentPayload(sourceType.getName(), element);
    }

    @Override
    public void removeCurrentPayload(SelectionFactory factory)
    {
        String name = sourceType.getName();
        factory.setCurrentPayload(name, previousPayload);
    }
}
