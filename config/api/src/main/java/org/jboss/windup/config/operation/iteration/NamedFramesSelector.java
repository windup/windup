/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.iteration;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.selectors.FramesSelector;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class NamedFramesSelector implements FramesSelector
{
    private final String varName;

    public NamedFramesSelector(String varName)
    {
        this.varName = varName;
    }

    @Override
    public Iterable<? extends WindupVertexFrame> getFrames(GraphRewrite event, EvaluationContext context)
                throws IllegalStateException
    {
        java.lang.Iterable<? extends WindupVertexFrame> result = Variables.instance(event).findVariable(varName);
        if (result == null)
            throw new IllegalStateException("No such variable [" + varName + "] was found in Variables stack.");
        return result;
    }

    @Override
    public String toString()
    {
        return getClass().getName() + " [" + varName + "]";
    }
}
