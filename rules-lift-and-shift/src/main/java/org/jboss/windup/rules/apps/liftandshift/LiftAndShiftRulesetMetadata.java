package org.jboss.windup.rules.apps.liftandshift;

import org.jboss.windup.config.WindupRulesetMetadata;

/**
 * Metadata for the Lift & Shift Ruleset.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class LiftAndShiftRulesetMetadata implements WindupRulesetMetadata
{
    public static final String RULE_SET_ID = "LiftAndShift";

    @Override
    public String getRuleSetID()
    {
        return RULE_SET_ID;
    }
}
