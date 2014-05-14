/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config;

import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.ContextBase;
import org.ocpsoft.rewrite.event.Flow;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class GraphRewrite implements Rewrite
{
    private final GraphContext graphContext;
    private final Context context = new ContextBase()
    {
    };

    public GraphRewrite(GraphContext context)
    {
        this.graphContext = context;
    }

    public void selectionPush()
    {
        SelectionFactory.instance(this).push();
    }

    public void selectionPop()
    {
        SelectionFactory.instance(this).pop();
    }

    public ResourceModel getResource()
    {
        return null;
    }

    @Override
    public Context getRewriteContext()
    {
        return context;
    }

    @Override
    public Flow getFlow()
    {
        return new Flow()
        {

            @Override
            public boolean isHandled()
            {
                return false;
            }

            @Override
            public boolean is(Flow type)
            {
                return false;
            }
        };
    }

    public GraphContext getGraphContext()
    {
        return graphContext;
    }
}
