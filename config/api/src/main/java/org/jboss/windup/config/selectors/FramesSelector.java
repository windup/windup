/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.selectors;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Retrieves proper Iterable of frames, from the variable stack.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface FramesSelector
{
    /**
     * Get the currently selected {@link WindupVertexFrame} instances for this {@link Iteration}.
     * 
     * @throws IllegalStateException upon failure to locate frames
     */
    Iterable<? extends WindupVertexFrame> getFrames(GraphRewrite event, EvaluationContext context);
}
