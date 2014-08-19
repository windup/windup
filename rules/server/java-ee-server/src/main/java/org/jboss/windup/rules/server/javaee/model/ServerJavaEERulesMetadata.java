package org.jboss.windup.rules.server.javaee.model;

import org.jboss.windup.config.WindupRuleMetadata;

/**
 * Metadata regarding the "server-javaee" rules addon.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class ServerJavaEERulesMetadata implements WindupRuleMetadata
{
    public static final String RULE_SET_ID = "ServerJavaEERules";

    @Override
    public String getRuleSetID()
    {
        return RULE_SET_ID;
    }

}
