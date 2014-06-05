/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.ConfigurationProvider;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public abstract class WindupConfigurationProvider implements ConfigurationProvider<GraphContext>
{
    public abstract RulePhase getPhase();

    public List<Class<? extends WindupConfigurationProvider>> getDependencies()
    {
        return Collections.emptyList();
    }

    @Override
    public int priority()
    {
        return getPhase().getPriority();
    }

    @Override
    public boolean handles(Object payload)
    {
        return payload instanceof GraphContext;
    }

    @SafeVarargs
    protected final List<Class<? extends WindupConfigurationProvider>> generateDependencies(
                Class<? extends WindupConfigurationProvider>... deps)
    {
        return Arrays.asList(deps);
    }
}
