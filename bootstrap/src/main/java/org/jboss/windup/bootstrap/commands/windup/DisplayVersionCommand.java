package org.jboss.windup.bootstrap.commands.windup;

import org.jboss.windup.bootstrap.Bootstrap;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;

public class DisplayVersionCommand implements Command
{

    @Override
    public CommandResult execute()
    {
        System.out.println(Bootstrap.getVersionString());
        return CommandResult.EXIT;
    }

    @Override
    public CommandPhase getPhase()
    {
        return CommandPhase.PRE_CONFIGURATION;
    }

}
