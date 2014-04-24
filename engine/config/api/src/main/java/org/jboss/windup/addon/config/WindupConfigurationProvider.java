/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config;

import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.ConfigurationProvider;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public abstract class WindupConfigurationProvider implements ConfigurationProvider<GraphContext>
{
    @Override
    public int priority()
    {
        return 0;
    }

    @Override
    public boolean handles(Object payload)
    {
        return payload instanceof GraphContext;
    }
}
