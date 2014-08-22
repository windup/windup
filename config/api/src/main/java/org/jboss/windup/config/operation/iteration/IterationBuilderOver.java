/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.iteration;

import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Intermediate step to construct an {@link Iteration}.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface IterationBuilderOver extends IterationBuilderVar
{
    /**
     * Sets the name and type of the variable for this iteration's "current element". The type server for automatic type
     * check.
     */
    public IterationBuilderVar as(Class<? extends WindupVertexFrame> varType, String var);

    /**
     * Sets the name of the variable for this iteration's "current element".
     */
    public IterationBuilderVar as(String var);

}
