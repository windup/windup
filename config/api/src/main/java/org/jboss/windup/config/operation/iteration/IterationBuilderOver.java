/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.iteration;

import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public interface IterationBuilderOver
{
    /**
     * Configure the iteration variable.
     */
    public IterationBuilderVar var(Class<? extends WindupVertexFrame> varType, String var);

    /**
     * Configure the iteration variable.
     */
    public IterationBuilderVar var(String var);

    /**
     * Iterate over the results of a query
     */
    public IterationQuery queryFor(Class<? extends WindupVertexFrame> varType, String var);

    /**
     * Iterate over the results of a query
     */
    public IterationQuery queryFor(String var);
}
