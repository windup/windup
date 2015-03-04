package org.jboss.windup.rules.apps.legacy.java;

import javax.inject.Inject;

import org.jboss.forge.furnace.addons.Addon;
import org.jboss.windup.config.metadata.AbstractRulesetMetadata;
import org.jboss.windup.config.metadata.RulesetMetadata;

/**
 * Basic metadata for the Java EE rule addon.
 *
 * @author jsightler <jesse.sightler@gmail.com>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class JavaEERulesetMetadata extends AbstractRulesetMetadata implements RulesetMetadata
{
    public static final String RULE_SET_ID = "CoreJavaEERules";

    @Inject
    public JavaEERulesetMetadata(Addon addon)
    {
        super(addon);
    }

    @Override
    public String getID()
    {
        return RULE_SET_ID;
    }
}