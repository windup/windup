/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.iteration;

import org.jboss.windup.config.operation.Iteration;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.Operation;

/**
 * Intermediate step to construct an {@link Iteration}.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface IterationBuilderVar
{

    /**
     * A condition which decides for each frame whether {@link #perform(Operation)} or
     * {@link Iteration#otherwise(Operation)} will be processed.
     */
    IterationBuilderWhen when(Condition condition);

    /**
     * Perform the given {@link Operation} when the conditions set in this {@link Iteration} are met.
     */
    IterationBuilderPerform perform(Operation operation);

    /**
     * Perform the given {@link Operation} instances when the conditions set in this {@link Iteration} are met.
     */
    IterationBuilderPerform perform(Operation... operations);

}
