package org.jboss.windup.config.metadata;

import java.util.List;

import org.jboss.forge.furnace.addons.Addon;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.RulePhase;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Each {@link Addon} that contains {@link RuleProvider} implementations should implement this interface, and by doing
 * so provide some basic metadata about its contents.
 * 
 * @author Jess Sightler <jesse.sightler@gmail.com>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface RulesetMetadata
{
    /**
     * Returns a unique identifier for the corresponding {@link RuleProvider}. The default is based on the originating
     * {@link Addon} and {@link Class} name, but this can be overridden in subclasses to provide a more readable name.
     */
    public String getID();

    /**
     * Returns a descriptive {@link String}, informing a human where they can find the {@link Rule} instances provided
     * by this {@link RuleProvider}.
     */
    public String getOrigin();

    /**
     * Return the {@link RulePhase} in which {@link Rule} instances from this {@link RuleProvider} should be executed.
     * <p>
     * The default phase is {@link RulePhase#MIGRATION_RULES}.
     */
    public Class<? extends RulePhase> getPhase();

    /**
     * Returns a list of {@link RuleProvider} classes that should execute before the {@link Rule} instances in this
     * corresponding {@link RuleProvider}.
     *
     * {@link RuleProvider}s can also be specified based on id ({@link #getExecuteAfterID}).
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
     * Returns a list of {@link RuleProvider} classes that should execute after the {@link Rule}s in this
     * {@link RuleProvider}.
     *
     * {@link RuleProvider}s can also be specified based on id ({@link #getExecuteBeforeID}).
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
}
