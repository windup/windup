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
public class TypedNamedIterationPayloadManager implements IterationPayloadManager
{

    private final Class<? extends WindupVertexFrame> varType;
    private final String var;

    public TypedNamedIterationPayloadManager(Class<? extends WindupVertexFrame> varType, String var)
    {
        this.varType = varType;
        this.var = var;
    }

    @Override
    public void setCurrentPayload(SelectionFactory factory, WindupVertexFrame element)
    {
        // TODO verify type
        factory.setCurrentPayload(var, element);
    }

    @Override
    public void removeCurrentPayload(SelectionFactory factory)
    {
        // TODO verify type
        factory.setCurrentPayload(var, null);
    }

}
