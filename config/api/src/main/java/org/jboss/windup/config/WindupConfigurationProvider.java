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

import javax.inject.Inject;

import org.jboss.forge.furnace.addons.Addon;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.ConfigurationProvider;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public abstract class WindupConfigurationProvider implements ConfigurationProvider<GraphContext>
{
    @Inject
    private Addon addon; // The current addon

    public String getID()
    {
        return addon.getId().getName() + "." + getClass().getName();
    }

    public abstract RulePhase getPhase();

    /**
     * Returns a list of WindupConfigurationProvider classes that this instance depends on.
     * 
     * Dependencies can also be specified based on id ({@link #getIDDependencies}).
     * 
     * @return
     */
    public List<Class<? extends WindupConfigurationProvider>> getClassDependencies()
    {
        return Collections.emptyList();
    }

    /**
     * Returns a list of the WindupConfigurationProvider dependencies for this configuration provider.
     * 
     * This is returned as a list of Rule IDs in order to support extensions that cannot depend on each other via class
     * names. For example, in the case of the Groovy rules extension, a single class covers many rules with their own
     * IDs.
     * 
     * For depending upon Java-based rules, getClassDependencies is preferred. Dependencies of both types can be
     * returned by a single WindupConfigurationProvider.
     * 
     * @return
     */
    public List<String> getIDDependencies()
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

    @SafeVarargs
    protected final List<String> generateDependencies(
                String... deps)
    {
        return Arrays.asList(deps);
    }
}
