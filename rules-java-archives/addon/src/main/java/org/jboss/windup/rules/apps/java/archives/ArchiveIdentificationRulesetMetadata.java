package org.jboss.windup.rules.apps.java.archives;

import javax.inject.Inject;

import org.jboss.forge.furnace.addons.Addon;
import org.jboss.windup.config.metadata.AbstractRulesetMetadata;

/**
 * MetadataBuilder for the Java Archive Identification addon.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ArchiveIdentificationRulesetMetadata extends AbstractRulesetMetadata
{
    public static final String RULE_SET_ID = "JavaArchiveIdentification";

    @Inject
    public ArchiveIdentificationRulesetMetadata(Addon addon)
    {
        super(addon);
    }

    @Override
    public String getID()
    {
        return RULE_SET_ID;
    }
}
