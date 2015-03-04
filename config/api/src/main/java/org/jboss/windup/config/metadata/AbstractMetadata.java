package org.jboss.windup.config.metadata;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.RulePhase;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.ContextBase;

/**
 * Base class for constructing {@link RulesetMetadata} instances. Provides sensible defaults.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class AbstractMetadata extends ContextBase implements RulesetMetadata
{
    public static final Class<? extends RulePhase> DEFAULT_PHASE = MigrationRulesPhase.class;

    private String id;

    public AbstractMetadata(String id)
    {
        Assert.notNull(id, "Ruleset ID must not be null.");
        this.id = id;
    }

    /**
     * Specify additional meta-data to individual {@link Rule} instances originating from the corresponding
     * {@link RuleProvider} instance.
     */
    public void enhanceRuleMetadata(Rule rule)
    {
        if (rule instanceof Context)
        {
            Context context = (Context) rule;
            if (!context.containsKey(RuleMetadataTypes.ORIGIN))
                context.put(RuleMetadataTypes.ORIGIN, this.getClass().getName());
            if (!context.containsKey(RuleMetadataTypes.RULE_PROVIDER))
                context.put(RuleMetadataTypes.RULE_PROVIDER, this);
        }
    }

    /**
     * Returns a unique identifier for the corresponding {@link RuleProvider} instance.
     */
    @Override
    public String getID()
    {
        return id;
    }

    /**
     * Provides descriptive information indicating where the corresponding {@link RuleProvider} instance is located (eg,
     * a path to an XML file on disk, or an {@link Addon} coordinate and class name).
     */
    @Override
    public String getOrigin()
    {
        return getID();
    }

    /**
     * Return the {@link RulePhase} in which the {@link Rule} instances from the corresponding {@link RuleProvider}
     * instance should be executed.
     * <p>
     * The default phase is {@link RulePhase#MIGRATION_RULES}.
     */
    public Class<? extends RulePhase> getPhase()
    {
        return DEFAULT_PHASE;
    }

    /**
     * Returns a list of {@link RuleProvider} classes that should execute before the {@link Rule} instances in the
     * corresponding {@link RuleProvider} instance.
     *
     * <p>
     * {@link RuleProvider} references can also be specified based on id ({@link #getExecuteAfterID}).
     */
    @Override
    public List<Class<? extends RuleProvider>> getExecuteAfter()
    {
        return Collections.emptyList();
    }

    /**
     * Returns a list of the {@link RuleProvider} classes that should execute before the {@link Rule} instances in the
     * corresponding {@link RuleProvider} instance.
     *
     * <p>
     * This is returned as a list of Rule IDs in order to support extensions that cannot depend on each other via class
     * names. For example, in the case of the Groovy rules extension, a single class covers many rules with their own
     * IDs.
     *
     * For specifying Java-based rules, {@link #getExecuteAfter()} is preferred.
     */
    @Override
    public List<String> getExecuteAfterIDs()
    {
        return Collections.emptyList();
    }

    /**
     * Returns a list of {@link RuleProvider} classes that should execute after the {@link Rule} instances in the
     * corresponding {@link RuleProvider} instance.
     *
     * {@link RuleProvider}s can also be specified based on id ({@link #getExecuteBeforeID}).
     */
    @Override
    public List<Class<? extends RuleProvider>> getExecuteBefore()
    {
        return Collections.emptyList();
    }

    /**
     * Returns a list of the {@link RuleProvider} classes that should execute after the {@link Rule} instances in the
     * corresponding {@link RuleProvider} instance.
     *
     * <p>
     * This is returned as a list of Rule IDs in order to support extensions that cannot depend on each other via class
     * names. For example, in the case of the Groovy rules extension, a single class covers many rules with their own
     * IDs.
     *
     * For specifying Java-based rules, {@link #getExecuteBefore()} is preferred.
     */
    @Override
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
    protected final List<Class<? extends RuleProvider>> asClassList(Class<? extends RuleProvider>... deps)
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

}
