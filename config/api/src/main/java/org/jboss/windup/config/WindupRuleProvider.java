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
import org.jboss.windup.config.phase.MigrationRules;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.ConfigurationProvider;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;

/**
 * {@link WindupRuleProvider} provides metadata, and a list of {@link Rule} objects that are then evaluated by the {@link RuleSubet} during Windup
 * execution.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class WindupRuleProvider implements ConfigurationProvider<GraphContext>
{
    public static final Class<? extends RulePhase> DEFAULT_PHASE = MigrationRules.class;

    @Inject
    private Addon addon;

    private int executionIndex;

    /**
     * Returns a unique identifier for this particular rule provider. The default is based on the addon and classname, but this can be overridden in
     * subclasses to provide a more readable name.
     */
    public String getID()
    {
        return addon.getId().getName() + "." + getClass().getSimpleName();
    }

    /**
     * Return the {@link RulePhase} in which the rules from this provider should be executed.
     * <p>
     * The default phase is {@link RulePhase#MIGRATION_RULES}.
     */
    public Class<? extends RulePhase> getPhase()
    {
        return DEFAULT_PHASE;
    }

    /**
     * Specify additional meta-data about the {@link Rule} instances originating from this {@link WindupRuleProvider}.
     */
    public void enhanceMetadata(Context context)
    {
        if (!context.containsKey(RuleMetadata.CATEGORY))
            context.put(RuleMetadata.CATEGORY, "Uncategorized");
        if (!context.containsKey(RuleMetadata.ORIGIN))
            context.put(RuleMetadata.ORIGIN, this.getClass().getName());
        if (!context.containsKey(RuleMetadata.RULE_PROVIDER))
            context.put(RuleMetadata.RULE_PROVIDER, this);
    }

    /**
     * Returns a list of {@link WindupRuleProvider} classes that should execute before the {@link Rule}s in this {@link WindupRuleProvider}.
     *
     * {@link WindupRuleProvider}s can also be specified based on id ({@link #getExecuteAfterID}).
     */
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return Collections.emptyList();
    }

    /**
     * Returns a list of the {@link WindupRuleProvider} classes that should execute before the {@link Rule}s in this {@link WindupRuleProvider}.
     *
     * This is returned as a list of Rule IDs in order to support extensions that cannot depend on each other via class names. For example, in the
     * case of the Groovy rules extension, a single class covers many rules with their own IDs.
     *
     * For specifying Java-based rules, getExecuteAfter is preferred.
     */
    public List<String> getExecuteAfterIDs()
    {
        return Collections.emptyList();
    }

    /**
     * Returns a list of {@link WindupRuleProvider} classes that should execute after the {@link Rule}s in this {@link WindupRuleProvider}.
     *
     * {@link WindupRuleProvider}s can also be specified based on id ({@link #getExecuteBeforeID}).
     */
    public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
    {
        return Collections.emptyList();
    }

    /**
     * Returns a list of the {@link WindupRuleProvider} classes that should execute after the {@link Rule}s in this {@link WindupRuleProvider}.
     *
     * This is returned as a list of Rule IDs in order to support extensions that cannot depend on each other via class names. For example, in the
     * case of the Groovy rules extension, a single class covers many rules with their own IDs.
     *
     * For specifying Java-based rules, getExecuteBefore is preferred.
     */
    public List<String> getExecuteBeforeIDs()
    {
        return Collections.emptyList();
    }

    /**
     * Convenience method for generating a list of classes based upon the passed parameters.
     *
     * For: generateDependencies(Foo.class, Bar.class, Baz.class) will return a List containing these three elements.
     */
    @SafeVarargs
    protected final List<Class<? extends WindupRuleProvider>> asClassList(
                Class<? extends WindupRuleProvider>... deps)
    {
        return Arrays.asList(deps);
    }

    /**
     * Convenience method for generating a list of Strings based upon the passed parameters.
     *
     * For: generateDependencies("Foo", "Bar", "Baz") will return a List containing these three elements.
     */
    @SafeVarargs
    protected final List<String> asStringList(
                String... deps)
    {
        return Arrays.asList(deps);
    }

    /**
     * The "priority" of the RuleProvider. This is not presently used by Windup.
     */
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

    @Override
    public boolean equals(Object other)
    {
        boolean result = false;
        if (other instanceof WindupRuleProvider)
        {
            WindupRuleProvider that = (WindupRuleProvider) other;
            result = this.getID().equals(that.getID());
        }
        return result;
    }

    public int hashCode()
    {
        return getID().hashCode();
    }

    public int getExecutionIndex()
    {
        return executionIndex;
    }

    public void setExecutionIndex(int executionIndex)
    {
        this.executionIndex = executionIndex;
    }
}
