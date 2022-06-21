package org.jboss.windup.bootstrap.commands.windup;

import org.jboss.windup.bootstrap.Bootstrap;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;

public class DisplayVersionCommand implements Command {
    private final CommandResult result;

    public DisplayVersionCommand() {
        result = CommandResult.EXIT;
    }

    public DisplayVersionCommand(CommandResult result) {
        this.result = result;
    }

    @Override
    public CommandResult execute() {
        System.out.println(Bootstrap.getVersionString());
        return result;
    }

    @Override
    public CommandPhase getPhase() {
        return CommandPhase.PRE_CONFIGURATION;
    }

}
