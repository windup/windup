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
import org.apache.commons.lang.StringUtils;

import org.jboss.forge.furnace.addons.Addon;
import org.jboss.windup.config.metadata.Rules;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.ConfigurationProvider;
import org.ocpsoft.rewrite.context.Context;

/**
 * {@link WindupRuleProvider} provides metadata, and a list of {@link Rule}s
 * that are then evaluated by the {@link RuleSubset} during Windup execution.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class WindupRuleProvider implements ConfigurationProvider<GraphContext>
{
    public static final Class<? extends RulePhase> DEFAULT_PHASE = MigrationRulesPhase.class;
    public static final Class<? extends RulePhase> DEFAULT_PHASE = MigrationRules.class;
    private static final String DEFAULT_CATEGORY = "Uncategorized";


    @Inject
    private Addon addon;

    private int executionIndex;

    /**
     * Provides descriptive information indicating where this rule provider was located (eg, a path to a groovy file on disk, or an addon coordinate
     * and class name).
     */
    public String getOrigin()
    {
        return addon.getId().getName() + ":" + getClass().getCanonicalName();
    }

    /**
     * Returns a unique identifier for this particular rule provider. The default is based on the addon and classname, but this can be overridden in
     * subclasses to provide a more readable name.
     */
    public String getID()
    {
        // TODO: Also take parent classes into account.
        Rules ann = this.getClass().getAnnotation(Rules.class);
        if(ann != null && ann.id().isEmpty())
            return ann.id();
        return addon.getId().getName() + "." + getClass().getSimpleName();
    }

    /**
     * Return the {@link RulePhase} in which the rules from this provider should be executed.
     * <p>
     * The default phase is {@link RulePhase#MIGRATION_RULES}.
     */
    public Class<? extends RulePhase> getPhase()
    {
        Rules rulesAnnotation = this.getClass().getAnnotation(Rules.class);
        if(rulesAnnotation == null)
            return DEFAULT_PHASE;
        if(rulesAnnotation.phase() == null)
            return DEFAULT_PHASE;
        return rulesAnnotation.phase();
    }

    /**
     * Specify additional meta-data about the {@link Rule}s instances originating from this {@link WindupRuleProvider}.
     */
    public void enhanceMetadata(Context context)
    {
        Rules rulesAnnotation = this.getClass().getAnnotation(Rules.class);
        if(rulesAnnotation != null){
            if(rulesAnnotation.categories().length != 0){
                String cats = StringUtils.join(rulesAnnotation.categories(), ','); // Until WINDUP-402
                context.put(RuleMetadata.CATEGORY, cats);
            }
            if(!rulesAnnotation.origin().isEmpty())
                context.put(RuleMetadata.ORIGIN, rulesAnnotation.origin());
        }

        // If neither annotations nor the Windup core set those, use the defaults.
        if (!context.containsKey(RuleMetadata.CATEGORY))
            context.put(RuleMetadata.CATEGORY, DEFAULT_CATEGORY);
        if (!context.containsKey(RuleMetadata.ORIGIN))
            context.put(RuleMetadata.ORIGIN, this.getClass().getName());

        if (!context.containsKey(RuleMetadata.RULE_PROVIDER))
            context.put(RuleMetadata.RULE_PROVIDER, this);
    }

    /**
     * Returns a list of {@link WindupRuleProvider} classes that should execute before the {@link Rule}s in this
     * {@link WindupRuleProvider}.
     *
     * {@link WindupRuleProvider}s can also be specified based on id ({@link #getExecuteAfterID}).
     */
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        Rules rulesAnnotation = this.getClass().getAnnotation(Rules.class);
        if(rulesAnnotation != null)
            return Arrays.asList(rulesAnnotation.after());
        else
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
        Rules rulesAnnotation = this.getClass().getAnnotation(Rules.class);
        if(rulesAnnotation != null)
            return Arrays.asList(rulesAnnotation.afterIDs());
        else
            return Collections.emptyList();
    }

    /**
     * Returns a list of {@link WindupRuleProvider} classes that should execute after the {@link Rule}s in this {@link WindupRuleProvider}.
     *
     * {@link WindupRuleProvider}s can also be specified based on id ({@link #getExecuteBeforeID}).
     */
    public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
    {
        Rules rulesAnnotation = this.getClass().getAnnotation(Rules.class);
        if(rulesAnnotation != null)
            return Arrays.asList(rulesAnnotation.before());
        else
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
        Rules rulesAnnotation = this.getClass().getAnnotation(Rules.class);
        if(rulesAnnotation != null)
            return Arrays.asList(rulesAnnotation.beforeIDs());
        else
            return Collections.emptyList();
    }

    /**
     * Convenience method for generating a list of classes based upon the passed parameters.
     *
     * For: generateDependencies(Foo.class, Bar.class, Baz.class) will return a List containing these three elements.
     */
    @SafeVarargs
    protected final List<Class<? extends WindupRuleProvider>> asClassList(Class<? extends WindupRuleProvider>... deps)
    {
        return Arrays.asList(deps);
    }

    /**
     * Convenience method for generating a list of Strings based upon the passed parameters.
     *
     * For: generateDependencies("Foo", "Bar", "Baz") will return a List containing these three elements.
     */
    @SafeVarargs
    protected final List<String> asStringList(String... deps)
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

    @Override
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
