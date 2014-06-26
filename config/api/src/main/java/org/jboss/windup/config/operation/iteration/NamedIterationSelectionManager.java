/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.iteration;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.selectables.VarStack;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class NamedIterationSelectionManager implements IterationSelectionManager
{

    private final String varName;

    public NamedIterationSelectionManager(String varName)
    {
        this.varName = varName;
    }

    @Override
    public Iterable<WindupVertexFrame> getFrames(GraphRewrite event, VarStack varStack)
    {
        return varStack.findVariable(varName);
    }

}
