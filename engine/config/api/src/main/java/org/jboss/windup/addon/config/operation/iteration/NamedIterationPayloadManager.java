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
public class NamedIterationPayloadManager implements IterationPayloadManager
{
    private final String var;

    public NamedIterationPayloadManager(String var)
    {
        this.var = var;
    }

    @Override
    public void setCurrentPayload(SelectionFactory factory, WindupVertexFrame element)
    {
        factory.setCurrentPayload(var, element);
    }

    @Override
    public void removeCurrentPayload(SelectionFactory factory)
    {
        factory.setCurrentPayload(var, null);
    }

}
