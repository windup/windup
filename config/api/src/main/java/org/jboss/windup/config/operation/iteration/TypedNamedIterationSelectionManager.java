/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.iteration;

import java.util.Iterator;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.exception.IllegalTypeArgumentException;
import org.jboss.windup.config.selectables.VarStack;
import org.jboss.windup.graph.model.WindupVertexFrame;


/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class TypedNamedIterationSelectionManager implements IterationSelectionManager
{

    private final Class<? extends WindupVertexFrame> framesModel;
    private final String varName;

    public TypedNamedIterationSelectionManager(Class<? extends WindupVertexFrame> framesModel, String varName)
    {
        this.framesModel = framesModel;
        this.varName = varName;
    }

    @Override
    public Iterable<WindupVertexFrame> getFrames(GraphRewrite event, VarStack factory)
    {
        final Iterable<WindupVertexFrame> frames = factory.findVariable(varName);

        // Check the type.
        final Iterator<WindupVertexFrame> it = frames.iterator();
        if(it.hasNext()){
            final Class<? extends WindupVertexFrame> actualType = it.next().getClass();
            if( ! this.framesModel.isAssignableFrom( actualType ) )
                throw new IllegalTypeArgumentException(varName, this.framesModel, actualType);
        }
        return frames;
    }

}
