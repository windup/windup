package org.jboss.windup.config.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.RulePhase;
import org.ocpsoft.rewrite.config.Rule;

/**
 * A descriptor for {@link RuleProvider} metadata. Can be overridden by altering the {@link RuleProviderMetadata} directly.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RuleMetadata
{
    /**
     * Returns a unique identifier for this particular {@link RuleProvider}. The default is based on the {@link Class} name, but this can be
     * overridden here - or in subclasses - to provide a more readable name.
     */
    String id() default "";

    /**
     * Returns a human-readable text containing a description of the rules in this {@link RuleProvider}.
     */
    String description() default "";

    /**
     * String used to display the progress of rule execution to the user.
     */
    String perform() default "";

    /**
     * Return the {@link RulePhase} in which the {@link Rule} instances from this {@link RuleProvider} should be executed.
     * <p>
     * The default phase is {@link MigrationRulesPhase}.
     */
    Class<? extends RulePhase> phase() default MigrationRulesPhase.class;

    /**
     * Returns a list of {@link RuleProvider} after which the {@link Rule} instances supplied by this {@link RuleProvider} should be executed.
     */
    Class<? extends RuleProvider>[] after() default {};

    /**
     * Returns a list of {@link RuleProvider} IDs after which the {@link Rule} instances supplied by this {@link RuleProvider} should be executed.
     * <p>
     * This is returned as a list of {@link RuleProvider} IDs in order to support extensions that cannot depend on each other via class names.
     */
    String[] afterIDs() default {};

    /**
     * Returns a list of {@link RuleProvider} before which the {@link Rule} instances supplied by this {@link RuleProvider} should be executed.
     */
    Class<? extends RuleProvider>[] before() default {};

    /**
     * Returns a list of {@link RuleProvider} IDs before which the {@link Rule} instances supplied by this {@link RuleProvider} should be executed.
     * <p>
     * This is returned as a list of {@link RuleProvider} IDs in order to support extensions that cannot depend on each other via class names.
     */
    String[] beforeIDs() default {};

    /**
     * The tags describing this {@link RuleProvider}.
     */
    String[] tags() default {};

    /**
     * A list of source technologies that the annotated rules pertain to.
     */
    Technology[] sourceTechnologies() default {};

    /**
     * A list of target technologies that the annotated rules pertain to.
     */
    Technology[] targetTechnologies() default {};

    /**
     * Whether Windup should stop execution if this provider's rule execution ends with an exception.
     *
     * By default, the exceptions are only logged and the failing rule appears in report.
     * The rule itself is responsible for handling exceptions and storing them into the graph.
     */
    boolean haltOnException() default false;

    /**
     * Indicates whether or not the rules in this provider should override other rules.
     *
     * If this ruleprovider has the same ID as another rule provider, then any rules in this provider
     * will override rules from that base rule provider that have the same id.
     */
    boolean overrideProvider() default false;

    /**
     * If true, Windup will skip running this RuleProvider. Meant for development purposes.
     */
    boolean disabled() default false;
}