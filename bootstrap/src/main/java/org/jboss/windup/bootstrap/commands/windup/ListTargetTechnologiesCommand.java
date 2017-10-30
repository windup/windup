package org.jboss.windup.bootstrap.commands.windup;

import org.jboss.windup.bootstrap.commands.AbstractListCommand;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;
import org.jboss.windup.exec.configuration.options.TargetOption;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ListTargetTechnologiesCommand extends AbstractListCommand implements Command, FurnaceDependent
{
    @Override
    public CommandResult execute()
    {
        printValuesSorted("Available target technologies", getOptionValuesFromHelp(TargetOption.NAME));
        return CommandResult.EXIT;
    }

    @Override
    public CommandPhase getPhase()
    {
        return CommandPhase.PRE_EXECUTION;
    }

}
