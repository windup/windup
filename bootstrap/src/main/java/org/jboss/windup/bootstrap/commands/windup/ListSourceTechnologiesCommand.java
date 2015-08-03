package org.jboss.windup.bootstrap.commands.windup;

import org.jboss.windup.bootstrap.commands.AbstractListCommand;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;
import org.jboss.windup.config.metadata.RuleProviderRegistryCache;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ListSourceTechnologiesCommand extends AbstractListCommand implements Command, FurnaceDependent
{
    @Override
    public CommandResult execute()
    {
        RuleProviderRegistryCache ruleProviderRegistryCache = getFurnace().getAddonRegistry().getServices(RuleProviderRegistryCache.class).get();
        printValuesSorted("Available source technologies", ruleProviderRegistryCache.getAvailableTargetTechnologies());
        return CommandResult.EXIT;
    }

    @Override
    public CommandPhase getPhase()
    {
        return CommandPhase.PRE_EXECUTION;
    }
}
