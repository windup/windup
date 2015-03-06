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
 * @author Ondrej Zizka, ozizka at redhat.com
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
}