package org.jboss.windup.rules.apps.javadecompiler;

import org.jboss.windup.config.WindupRuleMetadata;

/**
 * Defines the metadata for the decompiler addon
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class DecompilerRuleMetadata implements WindupRuleMetadata
{
    public static final String RULE_SET_ID = "JavaDecompiler";

    @Override
    public String getRuleSetID()
    {
        return RULE_SET_ID;
    }

}
