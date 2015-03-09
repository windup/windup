package org.jboss.windup.rules.apps.java;

import javax.inject.Inject;

import org.jboss.forge.furnace.addons.Addon;
import org.jboss.windup.config.metadata.AbstractRulesetMetadata;

/**
 * MetadataBuilder for the Java Metadata addon.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class JavaRulesetMetadata extends AbstractRulesetMetadata
{
    public static final String RULE_SET_ID = "CoreJavaRules";

    @Inject
    public JavaRulesetMetadata(Addon addon)
    {
        super(addon);
    }

    @Override
    public String getID()
    {
        return RULE_SET_ID;
    }
}
