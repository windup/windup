package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.metadata.AbstractRulesetMetadata;
import org.jboss.windup.config.metadata.RulesetMetadata;

/**
 * Basic metadata for the Java EE rule addon.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class JavaEERulesetMetadata extends AbstractRulesetMetadata implements RulesetMetadata {
    public static final String RULE_SET_ID = "CoreJavaEERules";

    public JavaEERulesetMetadata() {
        super(RULE_SET_ID);
    }
}