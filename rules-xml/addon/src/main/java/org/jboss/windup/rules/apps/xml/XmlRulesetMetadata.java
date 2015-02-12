package org.jboss.windup.rules.apps.xml;

import org.jboss.windup.config.WindupRulesetMetadata;

/**
 * Metadata for the XML Rules addon.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class XmlRulesetMetadata implements WindupRulesetMetadata
{
    public static final String RULE_SET_ID = "CoreXMLRules";

    @Override
    public String getRuleSetID()
    {
        return RULE_SET_ID;
    }
}
