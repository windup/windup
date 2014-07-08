/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.iteration;

import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.Operation;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface IterationQueryCriteria
{
    IterationBuilderWhen when(Condition condition);

    IterationBuilderPerform perform(Operation operation);
}
