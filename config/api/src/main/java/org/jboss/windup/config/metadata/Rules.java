package org.jboss.windup.config.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.phase.Implicit;
import org.jboss.windup.config.phase.RulePhase;

/**
 * A descriptor for {@link WindupRuleProvider} metadata. Can be overridden by overriding methods of {@link WindupRuleProvider}
 * like <code>getExecuteAfter()</code>, <code>getExecuteAfterIDs()</code>, <code>getID()</code> or <code>enhanceMetadata()</code>.
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rules
{
    /**
     * Returns a unique identifier for this particular rule provider.
     * The default is based on the addon and classname,
     * but this can be overridden here or in subclasses to provide a more readable name.
     */
    String id() default "";

    /**
     * Return the {@link RulePhase} in which the rules from this provider should be executed.
     * <p>
     * The default phase is {@link RulePhase#MIGRATION_RULES}.
     */
    //RulePhase phase() default WindupRuleProvider.DEFAULT_PHASE;
    Class<? extends RulePhase> phase() default Implicit.class;


    /**
     * Returns a list of {@link WindupRuleProvider} classes that should execute before the {@link Rules}s in this
     * {@link WindupRuleProvider}.
     *
     * {@link WindupRuleProvider}s can also be specified based on id ({@link #getExecuteAfterID}).
     */
    Class<? extends WindupRuleProvider>[] after() default {};


    /**
     * Returns a list of the {@link WindupRuleProvider} classes that should execute before the {@link Rules}s in this
     * {@link WindupRuleProvider}.
     *
     * This is returned as a list of Rules IDs in order to support extensions that cannot depend on each other via class
 names. For example, in the case of the Groovy rules extension, a single class covers many rules with their own
 IDs.

 For specifying Java-based rules, getExecuteAfter is preferred.
     */
    String[] afterIDs() default {};


    /**
     * Returns a list of {@link WindupRuleProvider} classes that should execute after the {@link Rules}s in this
     * {@link WindupRuleProvider}.
     *
     * {@link WindupRuleProvider}s can also be specified based on id ({@link #getExecuteBeforeID}).
     */
    Class<? extends WindupRuleProvider>[] before() default {};


    /**
     * Returns a list of the {@link WindupRuleProvider} classes that should execute after the {@link Rules}s in this
     * {@link WindupRuleProvider}.
     *
     * This is returned as a list of Rules IDs in order to support extensions that cannot depend on each other via class
 names. For example, in the case of the Groovy rules extension, a single class covers many rules with their own
 IDs.

 For specifying Java-based rules, getExecuteBefore is preferred.
     */
    String[] beforeIDs() default {};


    /**
     * Categories to store in RuleMetadata.CATEGORY.
     */
    String[] categories() default {"Uncategorized"};


    /**
     * Declaration of origin to store in RuleMetadata.ORIGIN. Typically the Rules class or the XML file.
     */
    String origin() default "";

}
