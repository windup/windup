package org.jboss.windup.rules.apps.java;

import org.jboss.windup.config.metadata.RulesetMetadata;

/**
 * MetadataBuilder for the Java Rules addon.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class JavaRulesetMetadata implements RulesetMetadata
{
    public static final String RULE_SET_ID = "CoreJavaRules";

    @Override
    public String getRuleSetID()
    {
        return RULE_SET_ID;
    }
}
