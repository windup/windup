package org.jboss.windup.config.metadata;

import org.jboss.forge.furnace.addons.Addon;

/**
 * Base class for constructing {@link RulesetMetadata} instances for Windup rulesets. Provides sensible defaults.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class AbstractRulesetMetadata extends AbstractMetadata

{
    /**
     * Construct a new {@link AbstractRulesetMetadata} instance.
     */
    public AbstractRulesetMetadata(Addon addon)
    {
        super(addon.getId().toCoordinates());
    }
}
