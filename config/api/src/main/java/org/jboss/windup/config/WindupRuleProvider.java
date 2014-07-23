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
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.log.jul.config.Logging;
import org.ocpsoft.rewrite.config.ConfigurationProvider;
import org.ocpsoft.rewrite.context.Context;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public abstract class WindupRuleProvider implements ConfigurationProvider<GraphContext>
{
    @Inject
    private Addon addon;

    public String getID()
    {
        return addon.getId().getName() + "." + getClass().getName();
    }

    /**
     * Return the {@link RulePhase} in which the rules from this provider should be executed.
     */
    public abstract RulePhase getPhase();

    /**
     * Specify additional meta-data about the {@link Rule} instances originating from this {@link WindupRuleProvider}.
     */
    public void enhanceMetadata(Context context)
    {
        if (!context.containsKey(RuleMetadata.CATEGORY))
            context.put(RuleMetadata.CATEGORY, "none");
        if (!context.containsKey(RuleMetadata.ORIGIN))
            context.put(RuleMetadata.ORIGIN, this.getClass().getName());
    }

    /**
     * Returns a list of WindupRuleProvider classes that this instance depends on.
     * 
     * Dependencies can also be specified based on id ({@link #getIDDependencies}).
     */
    public List<Class<? extends WindupRuleProvider>> getClassDependencies()
    {
        return Collections.emptyList();
    }

    /**
     * Returns a list of the WindupRuleProvider dependencies for this configuration provider.
     * 
     * This is returned as a list of Rule IDs in order to support extensions that cannot depend on each other via class
     * names. For example, in the case of the Groovy rules extension, a single class covers many rules with their own
     * IDs.
     * 
     * For depending upon Java-based rules, getClassDependencies is preferred. Dependencies of both types can be
     * returned by a single WindupRuleProvider.
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
    protected final List<Class<? extends WindupRuleProvider>> generateDependencies(
                Class<? extends WindupRuleProvider>... deps)
    {
        return Arrays.asList(deps);
    }

    @SafeVarargs
    protected final List<String> generateDependencies(
                String... deps)
    {
        return Arrays.asList(deps);
    }
    
    static {
        Logging.init();
    }
}
