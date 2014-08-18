package org.jboss.windup.rules.apps.java;

import org.jboss.windup.config.WindupRuleMetadata;

/**
 * Metadata for the Java Rules addon.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class JavaRulesMetadata implements WindupRuleMetadata
{
    public static final String RULE_SET_ID = "CoreJavaRules";

    @Override
    public String getRuleSetID()
    {
        return RULE_SET_ID;
    }
}
