/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.iteration;

import org.jboss.windup.config.Variables;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Sets or removes the current {@link Iteration} payload from the {@link Variables} stack.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface IterationPayloadManager
{
    
    /**
     * Get the name of the {@link Iteration} payload.
     */
    String getPayLoadName();
    
    /**
     * Set the current {@link Iteration} payload.
     */
    void setCurrentPayload(Variables varStack, WindupVertexFrame element);

    /**
     * Remove the current {@link Iteration} payload.
     */
    void removeCurrentPayload(Variables varStack);
}
