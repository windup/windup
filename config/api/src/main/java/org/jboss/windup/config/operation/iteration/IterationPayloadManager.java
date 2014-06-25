/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.iteration;

import org.jboss.windup.config.selectables.VarStack;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Sets or removes the current payload from the variable stack / payload manager (factory).
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 * TODO: Personally I'd remove the whole IterationPayloadManager interface
 * and access VarStack directly.
 */
public interface IterationPayloadManager
{
    void setCurrentPayload(VarStack factory, WindupVertexFrame element);

    void removeCurrentPayload(VarStack factory);
}
