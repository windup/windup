package org.jboss.windup.config.metadata;

import org.ocpsoft.rewrite.config.Rule;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public enum RuleMetadata
{
    /**
     * The {@link Rule} category.
     */
    CATEGORY,

    /**
     * The {@link Rule} origin.
     */
    ORIGIN,

    /**
     * Whether or not to call commit after each {@link Rule} execution.
     * 
     * The default behavior (if this is not set) is to autocommit after each rule execution.
     */
    AUTO_COMMIT,
}
