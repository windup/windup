package org.jboss.windup.rules.apps.java.archives;

import org.jboss.windup.config.metadata.RulesetMetadata;

/**
 * MetadataBuilder for the Java Archive Identification addon.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ArchiveIdentificationRulesetMetadata implements RulesetMetadata
{
    public static final String RULE_SET_ID = "JavaArchiveIdentification";

    @Override
    public String getRuleSetID()
    {
        return RULE_SET_ID;
    }
}
