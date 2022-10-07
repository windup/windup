package org.jboss.windup.rules.apps.java;

import org.jboss.windup.config.metadata.AbstractRulesetMetadata;

/**
 * MetadataBuilder for the Java RuleMetadata addon.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class JavaRulesetMetadata extends AbstractRulesetMetadata {
    public static final String RULE_SET_ID = "CoreJavaRules";

    public JavaRulesetMetadata() {
        super(RULE_SET_ID);
    }
}
