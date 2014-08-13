/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation;

import org.jboss.windup.config.GraphRewrite;
import org.ocpsoft.rewrite.config.DefaultOperationBuilder;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.OperationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public abstract class GraphOperation extends DefaultOperationBuilder
{
    
    @Override
    public OperationBuilder and(final Operation other)
    {
       if (other == null)
          return this;
       return new WindupOperationBuilder(this, other);
    }
    
    @Override
    public void perform(Rewrite event, EvaluationContext context)
    {
        if (event instanceof GraphRewrite)
            perform((GraphRewrite) event, context);
    }

    public abstract void perform(GraphRewrite event, EvaluationContext context);
}
