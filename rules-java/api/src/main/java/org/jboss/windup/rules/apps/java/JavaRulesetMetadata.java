package org.jboss.windup.rules.apps.java;

import org.jboss.windup.config.metadata.AbstractRulesetMetadata;

/**
 * MetadataBuilder for the Java RuleMetadata addon.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class JavaRulesetMetadata extends AbstractRulesetMetadata
{
    public static final String RULE_SET_ID = "CoreJavaRules";

    public JavaRulesetMetadata()
    {
        super(RULE_SET_ID);
    }
}
