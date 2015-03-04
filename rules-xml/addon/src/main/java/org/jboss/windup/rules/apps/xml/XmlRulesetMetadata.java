package org.jboss.windup.rules.apps.xml;

import javax.inject.Inject;

import org.jboss.forge.furnace.addons.Addon;
import org.jboss.windup.config.metadata.AbstractRulesetMetadata;

/**
 * MetadataBuilder for the XML Rules addon.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class XmlRulesetMetadata extends AbstractRulesetMetadata
{
    public static final String RULE_SET_ID = "CoreXMLRules";

    @Inject
    public XmlRulesetMetadata(Addon addon)
    {
        super(addon);
    }

    @Override
    public String getID()
    {
        return RULE_SET_ID;
    }
}
