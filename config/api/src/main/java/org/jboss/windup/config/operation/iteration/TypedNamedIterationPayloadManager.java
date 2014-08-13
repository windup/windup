/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.iteration;

import org.jboss.windup.config.Variables;
import org.jboss.windup.config.exception.IllegalTypeArgumentException;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Basically, a NamedIterationPayloadManager + a type check.
 * 
 * TODO: Personally I'd remove the whole IterationPayloadManager interface and access VarStack directly.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
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
    public void setCurrentPayload(Variables varStack, WindupVertexFrame element)
    {
        if (!varType.isAssignableFrom(element.getClass()))
        {
            throw new IllegalTypeArgumentException(var, varType, element.getClass());
        }
        Iteration.setCurrentPayload(varStack, var, element);
    }

    @Override
    public void removeCurrentPayload(Variables varStack)
    {
        Iteration.removeCurrentPayload(varStack, varType, var);
    }

    @Override
    public String getPayLoadName()
    {
        return var;
    }

}
