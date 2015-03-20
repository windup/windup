package org.jboss.windup.config.metadata;

import java.util.List;

import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.RulePhase;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Describes {@link RuleProvider} instances.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface RuleProviderMetadata extends RulesetMetadata
{
    /**
     * Returns the {@link Class} of the corresponding {@link RuleProvider}.
     */
    Class<? extends RuleProvider> getType();

    /**
     * Returns the {@link RulesetMetadata}, if any, for the rule-set from which this {@link RuleProviderMetadata}
     * originated.
     */
    RulesetMetadata getRulesetMetadata();

    /**
     * Return the {@link RulePhase} in which {@link Rule} instances from this {@link RuleProvider} should be executed.
     * <p>
     * The default phase is {@link org.jboss.windup.config.phase.MigrationRulesPhase}.
     */
    public Class<? extends RulePhase> getPhase();

    /**
     * Returns a list of {@link RuleProvider} classes that should execute before the {@link Rule} instances in this corresponding {@link RuleProvider}
     * .
     *
     * {@link RuleProvider}s can also be specified based on id ({@link #getExecuteAfterIDs}).
     */
    public List<Class<? extends RuleProvider>> getExecuteAfter();

    /**
     * Returns a list of the {@link RuleProvider} classes that should execute before the {@link Rule}s in this
     * {@link RuleProvider}.
     *
     * This is returned as a list of {@link Rule} IDs in order to support extensions that cannot depend on each other
     * via class names. For example, in the case of the Groovy rules extension, a single class covers many rules with
     * their own IDs.
     *
     * For specifying Java-based rules, {@link #getExecuteAfter()} is preferred.
     */
    public List<String> getExecuteAfterIDs();

    /**
     * Returns a list of {@link RuleProvider} classes that should execute after the {@link Rule}s in this {@link RuleProvider}.
     *
     * {@link RuleProvider}s can also be specified based on id ({@link #getExecuteBeforeIDs}).
     */
    public List<Class<? extends RuleProvider>> getExecuteBefore();

    /**
     * Returns a list of the {@link RuleProvider} classes that should execute after the {@link Rule}s in this
     * {@link RuleProvider}.
     *
     * This is returned as a list of {@link Rule} IDs in order to support extensions that cannot depend on each other
     * via {@link Class} names. For example, in the case of the Groovy rules extension, a single class covers many rules
     * with their own IDs.
     *
     * For specifying Java-based rules, {@link #getExecuteBefore()} is preferred.
     */
    public List<String> getExecuteBeforeIDs();


    /**
     * Whether Windup should stop execution if this provider's rule execution ends with an exception.
     *
     * By default, the exceptions are only logged and the failing rule appears in report.
     * The rule itself is responsible for handling exceptions and storing them into the graph.
     */
    public boolean isHaltOnException();

}
