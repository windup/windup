package org.jboss.windup.bootstrap.commands.windup;

import org.jboss.windup.bootstrap.commands.AbstractListCommandWithoutFurnace;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.exec.configuration.options.IncludeTagsOption;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ListTagsCommand extends AbstractListCommandWithoutFurnace implements Command
{
    @Override
    public CommandResult execute()
    {
        printValuesSorted("Available tags", getOptionValuesFromHelp(IncludeTagsOption.NAME));
        return CommandResult.EXIT;
    }

    @Override
    public CommandPhase getPhase()
    {
        return CommandPhase.PRE_CONFIGURATION;
    }

}
