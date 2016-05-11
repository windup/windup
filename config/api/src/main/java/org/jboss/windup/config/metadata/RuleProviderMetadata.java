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
    Class<? extends RulePhase> getPhase();

    /**
     * Returns a list of {@link RuleProvider} classes that should execute before the {@link Rule} instances in this corresponding {@link RuleProvider}
     * .
     *
     * {@link RuleProvider}s can also be specified based on id ({@link #getExecuteAfterIDs}).
     */
    List<Class<? extends RuleProvider>> getExecuteAfter();

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
    List<String> getExecuteAfterIDs();

    /**
     * Returns a list of {@link RuleProvider} classes that should execute after the {@link Rule}s in this {@link RuleProvider}.
     *
     * {@link RuleProvider}s can also be specified based on id ({@link #getExecuteBeforeIDs}).
     */
    List<Class<? extends RuleProvider>> getExecuteBefore();

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
    List<String> getExecuteBeforeIDs();


    /**
     * Whether Windup should stop execution if this provider's rule execution ends with an exception.
     *
     * By default, the exceptions are only logged and the failing rule appears in report.
     * The rule itself is responsible for handling exceptions and storing them into the graph.
     */
    boolean isHaltOnException();

    /**
     * Indicates whether or not the rules in this provider should override other rules.
     *
     * If this ruleprovider has the same ID as another rule provider, then any rules in this provider
     * will override rules from that base rule provider that have the same id.
     */
    boolean isOverrideProvider();

    /**
     * If true, Windup will skip running this RuleProvider. Meant for development purposes.
     */
    boolean isDisabled();
}
