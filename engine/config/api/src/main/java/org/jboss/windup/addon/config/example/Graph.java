/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.example;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.operation.GraphOperation;
import org.jboss.windup.addon.config.selectables.Selectable;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class Graph extends GraphOperation
{
    public static Graph replace(Selectable<?, ?> current)
    {
        return new Graph();
    }

    public Graph with(Selectable<?, ?> current)
    {
        return this;
    }

    public static Graph insert(MavenDependency current)
    {
        return new Graph();
    }

    public Graph to(MavenPomFile current)
    {
        return this;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {

    }
}
