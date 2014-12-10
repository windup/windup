package org.jboss.windup.rules.apps.legacy.java;
import org.jboss.windup.config.WindupRulesetMetadata;
/**
* Basic metadata for the Java EE rule addon.
*
* @author jsightler <jesse.sightler@gmail.com>
*
*/
public class JavaEERulesetMetadata implements WindupRulesetMetadata
{
public static final String RULE_SET_ID = "CoreJavaEERules";
@Override
public String getRuleSetID()
{
return RULE_SET_ID;
}
}