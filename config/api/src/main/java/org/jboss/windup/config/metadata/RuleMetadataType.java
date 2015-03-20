package org.jboss.windup.config.metadata;

import org.jboss.windup.config.AbstractRuleProvider;
import org.ocpsoft.rewrite.config.Rule;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public enum RuleMetadataType
{
    /**
     * The {@link AbstractRuleProvider} that originated this rule
     *//**
     * The {@link AbstractRuleProvider} that originated this rule
     */
    RULE_PROVIDER,

    /**
     * The {@link Rule} origin.
     */
    ORIGIN,

    /**
     * The tags describing this {@link Rule}.
     */
    TAGS,

    /**
     * Whether or not to call commit after each {@link Rule} execution.
     *
     * The default behavior (if this is not set) is to autocommit after each rule execution.
     */
    AUTO_COMMIT,

    /**
     * Whether Windup should stop execution if this provider's rule execution ends with an exception.
     *
     * By default, the exceptions are only logged and the failing rule appears in report.
     * The rule itself is responsible for handling exceptions and storing them into the graph.
     */
    HALT_ON_EXCEPTION
}
